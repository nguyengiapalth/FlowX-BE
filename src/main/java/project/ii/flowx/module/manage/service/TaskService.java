package project.ii.flowx.module.manage.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ii.flowx.applications.events.TaskEvent;
import project.ii.flowx.module.file.FileService;
import project.ii.flowx.module.auth.service.AuthorizationService;
import project.ii.flowx.applications.helper.EntityLookupService;
import project.ii.flowx.module.manage.entity.Task;
import project.ii.flowx.module.manage.TaskRepository;
import project.ii.flowx.module.user.repository.UserRepository;
import project.ii.flowx.module.file.dto.FileResponse;
import project.ii.flowx.module.manage.dto.task.TaskCreateRequest;
import project.ii.flowx.module.manage.dto.task.TaskResponse;
import project.ii.flowx.module.manage.dto.task.TaskUpdateRequest;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;
import project.ii.flowx.module.manage.mapper.TaskMapper;
import project.ii.flowx.security.UserPrincipal;
import project.ii.flowx.applications.enums.Visibility;
import project.ii.flowx.applications.enums.FileTargetType;
import project.ii.flowx.applications.enums.TaskStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TaskService {
    TaskRepository taskRepository;
    TaskMapper taskMapper;
    EntityLookupService entityLookupService;
    FileService fileService;
    AuthorizationService authorizationService;
    ApplicationEventPublisher eventPublisher;
    UserRepository userRepository;

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or @authorize.canCreateTask(#taskCreateRequest.targetId, #taskCreateRequest.targetType)")
    public TaskResponse createTask(TaskCreateRequest taskCreateRequest) {
        UUID userId = getUserId();
        
        // Validate assigneeId exists if provided
        if (taskCreateRequest.getAssigneeId() != null) {
            if (!userRepository.existsById(taskCreateRequest.getAssigneeId())) {
                throw new FlowXException(FlowXError.NOT_FOUND, 
                    "Assignee not found with ID: " + taskCreateRequest.getAssigneeId());
            }
        }

        // Create task using mapper and set assignerId
        Task task = taskMapper.toTask(taskCreateRequest);
        if (task.getStatus() == null) task.setStatus(TaskStatus.TO_DO);
        if (task.getHasFiles() == null) task.setHasFiles(false);
        
        // Set assignerId and assigneeId directly instead of User objects
        task.setAssignerId(userId); // Current user is the assigner
        task.setAssigneeId(taskCreateRequest.getAssigneeId()); // Can be null

        task = taskRepository.save(task);
        
//        // Publish task created event
//        eventPublisher.publishEvent(new TaskEvent.TaskCreatedEvent(
//            task.getId(),
//            userId,
//            task.getAssigneeId(),
//            task.getTitle(),
//            task.getTargetType().toString(),
//            task.getTargetId()
//        ));
        
        log.info("Created task {} by user {} assigned to {}", task.getId(), userId, task.getAssigneeId());
        
        return populateFiles(taskMapper.toTaskResponse(task));
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or @authorize.isTaskAssigner(#id) or @authorize.isTaskManager(#id)")
    public TaskResponse updateTask(UUID id, TaskUpdateRequest taskUpdateRequest) {
        Task task = getTaskByIdInternal(id);

        taskMapper.updateTaskFromRequest(task, taskUpdateRequest);
        task = taskRepository.save(task);
        
        // Publish task updated event
        UUID userId = getUserId();
        eventPublisher.publishEvent(new TaskEvent.TaskUpdatedEvent(task.getId(), userId, task.getTitle()));

        return populateFiles(taskMapper.toTaskResponse(task));
    }

    @Transactional
    public void updateHasFileFlag(UUID taskId) {
        try {
            Task task = getTaskByIdInternal(taskId);
            List<FileResponse> files = fileService.getFilesByTarget(FileTargetType.TASK, taskId);
            boolean hasFiles = !files.isEmpty();

            if (!java.util.Objects.equals(task.getHasFiles(), hasFiles)) {
                task.setHasFiles(hasFiles);
                taskRepository.save(task);
                log.info("Updated hasFiles flag for task {} to {}", taskId, hasFiles);
            }
        } catch (Exception e) {
            log.error("Failed to update hasFiles flag for task {}: {}", taskId, e.getMessage());
        }
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or @authorize.isTaskAssigner(#id)")
    public void deleteTask(UUID id) {
        Task task = getTaskByIdInternal(id);
        
        // Delete associated files first
        try {
            fileService.deleteFilesByTarget(FileTargetType.TASK, id);
        } catch (Exception e) {
            log.error("Failed to delete files for task {}: {}", id, e.getMessage());
        }
        
        taskRepository.delete(task);
        
        // Publish task deleted event
        UUID userId = getUserId();
        eventPublisher.publishEvent(new TaskEvent.TaskDeletedEvent(
            id, 
            userId, 
            task.getTitle()
        ));
        
        log.info("Deleted task {} by user {}", id, userId);
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or @authorize.isTaskAssignee(#taskId) or @authorize.isTaskManager(#taskId)")
    public TaskResponse updateTaskStatus(UUID taskId, TaskStatus status) {
        Task task = getTaskByIdInternal(taskId);
        TaskStatus oldStatus = task.getStatus();
        
        task.setStatus(status);
        
        // Set completed date if task is completed
        if (status == TaskStatus.COMPLETED && oldStatus != TaskStatus.COMPLETED) {
            task.setCompletedDate(LocalDate.now());
        } else if (status != TaskStatus.COMPLETED) {
            task.setCompletedDate(null);
        }
        
        task = taskRepository.save(task);
        
        // Publish status changed event
        UUID userId = getUserId();
        eventPublisher.publishEvent(new TaskEvent.TaskStatusChangedEvent(
            task.getId(), 
            userId, 
            oldStatus.toString(), 
            status.toString(),
            task.getTitle()
        ));
        
        log.info("Task {} status changed from {} to {} by user {}", 
            taskId, oldStatus, status, userId);
        
        return populateFiles(taskMapper.toTaskResponse(task));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authorize.canAccessTask(#id)")
    public TaskResponse getTaskById(UUID id) {
        Task task = getTaskByIdInternal(id);
        return populateFiles(taskMapper.toTaskResponse(task));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<TaskResponse> getTasksByProjectId(UUID projectId) {
        List<Task> tasks = taskRepository.findByTargetTypeAndTargetId(Visibility.PROJECT, projectId);
        return populateFilesList(taskMapper.toTaskResponseList(tasks));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<TaskResponse> getMyTaskCreated() {
        UUID userId = getUserId();
        List<Task> tasks = taskRepository.findByAssignerId(userId);
        return populateFilesList(taskMapper.toTaskResponseList(tasks));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<TaskResponse> getMyTask() {
        UUID userId = getUserId();
        List<Task> tasks = taskRepository.findByAssigneeId(userId);
        return populateFilesList(taskMapper.toTaskResponseList(tasks));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<TaskResponse> getMyTaskByProjectId(UUID projectId) {
        UUID userId = getUserId();
        List<Task> tasks = taskRepository.findByAssigneeIdAndTargetTypeAndTargetId(userId, Visibility.PROJECT, projectId);
        return populateFilesList(taskMapper.toTaskResponseList(tasks));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<TaskResponse> getTasksByStatus(TaskStatus status) {
        List<Task> tasks = taskRepository.findByStatus(status);
        return populateFilesList(taskMapper.toTaskResponseList(tasks));
    }

    // Helper methods
    private Task getTaskByIdInternal(UUID id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Task not found"));
    }

    private UUID getUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new FlowXException(FlowXError.UNAUTHENTICATED, "No authenticated user found");
        }
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userPrincipal.getId();
    }

    // For scheduled jobs
    public List<Task> getTasksDueToday() {
        return taskRepository.findTasksDueToday();
    }

    public List<Task> getOverdueTasks() {
        return taskRepository.findOverdueTasks();
    }
    
    public List<Task> getTasksDueInDays(int days) {
        return taskRepository.findTasksDueInDays(days);
    }

    private TaskResponse populateFiles(TaskResponse taskResponse) {
        if (taskResponse == null) return null;
        
        try {
            if (taskResponse.getHasFiles() != null && taskResponse.getHasFiles()) {
                List<FileResponse> files = fileService.getFilesByTarget(FileTargetType.TASK, taskResponse.getId());
                taskResponse.setFiles(files);
                
                if (files.isEmpty()) {
                    taskResponse.setHasFiles(false);
                    // Update database
                    Task task = taskRepository.findById(taskResponse.getId()).orElse(null);
                    if (task != null) {
                        task.setHasFiles(false);
                        taskRepository.save(task);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to populate files for task {}: {}", taskResponse.getId(), e.getMessage());
        }
        
        return taskResponse;
    }

    private List<TaskResponse> populateFilesList(List<TaskResponse> taskResponses) {
        return taskResponses.stream()
                .map(this::populateFiles)
                .collect(Collectors.toList());
    }
}
