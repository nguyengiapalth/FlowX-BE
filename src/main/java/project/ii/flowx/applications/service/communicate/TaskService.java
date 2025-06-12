package project.ii.flowx.applications.service.communicate;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ii.flowx.applications.service.FileService;
import project.ii.flowx.applications.service.auth.AuthorizationService;
import project.ii.flowx.applications.service.helper.EntityLookupService;
import project.ii.flowx.model.entity.Task;
import project.ii.flowx.model.entity.User;
import project.ii.flowx.model.repository.TaskRepository;
import project.ii.flowx.model.dto.file.FileResponse;
import project.ii.flowx.model.dto.task.TaskCreateRequest;
import project.ii.flowx.model.dto.task.TaskResponse;
import project.ii.flowx.model.dto.task.TaskUpdateRequest;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;
import project.ii.flowx.model.mapper.TaskMapper;
import project.ii.flowx.security.UserPrincipal;
import project.ii.flowx.shared.enums.ContentTargetType;
import project.ii.flowx.shared.enums.FileTargetType;
import project.ii.flowx.shared.enums.TaskStatus;

import java.util.ArrayList;
import java.util.List;

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

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or @authorize.canCreateTask(#taskCreateRequest.targetId, #taskCreateRequest.targetType)")
    public TaskResponse createTask(TaskCreateRequest taskCreateRequest) {
        Long userId = getUserId();
        User user = entityLookupService.getUserById(userId);

        Task task = taskMapper.toTask(taskCreateRequest);
        task.setIsCompleted(false);
        if (task.getStatus() == null) task.setStatus(TaskStatus.TO_DO);
        if (task.getHasFiles() == null) task.setHasFiles(false);
        task.setAssigner(user);

        task = taskRepository.save(task);

        return populateFiles(taskMapper.toTaskResponse(task));
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or @authorize.isTaskAssigner(#id) or @authorize.isTaskManager(#id)")
    public TaskResponse updateTask(Long id, TaskUpdateRequest taskUpdateRequest) {
        Task task = getTaskByIdInternal(id);
        taskMapper.updateTaskFromRequest(task, taskUpdateRequest);
        task = taskRepository.save(task);

        return populateFiles(taskMapper.toTaskResponse(task));
    }

    @Transactional
    @PreAuthorize("isAuthenticated()")
    public void updateHasFileFlag(Long taskId) {
        Task task = getTaskByIdInternal(taskId);
        List<FileResponse> files = fileService.getFilesByEntity(FileTargetType.TASK, taskId);
        task.setHasFiles(!files.isEmpty());
        taskRepository.save(task);
    }

    @Transactional
    @PreAuthorize("@authorize.isTaskAssignee(#id) or @authorize.isTaskManager(#id)")
    public TaskResponse updateTaskStatus(Long id, TaskStatus status) {

        Task task = getTaskByIdInternal(id);
        TaskStatus oldStatus = task.getStatus();
        task.setStatus(status);

        if (status == TaskStatus.COMPLETED) {
            task.setIsCompleted(true);
        }

        task = taskRepository.save(task);
        return populateFiles(taskMapper.toTaskResponse(task));
    }

    @Transactional
    @PreAuthorize("isAuthenticated()")
    public TaskResponse markTaskCompleted(Long id) {
        Task task = getTaskByIdInternal(id);
        task.setIsCompleted(true);
        task.setStatus(TaskStatus.COMPLETED);

        task = taskRepository.save(task);
        return populateFiles(taskMapper.toTaskResponse(task));
    }

    @Transactional
    @PreAuthorize("isAuthenticated()")
    public TaskResponse markTaskIncomplete(Long id) {
        Task task = getTaskByIdInternal(id);
        task.setIsCompleted(false);
        if (task.getStatus() == TaskStatus.COMPLETED) task.setStatus(TaskStatus.IN_PROGRESS);

        task = taskRepository.save(task);
        return populateFiles(taskMapper.toTaskResponse(task));
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or @authorize.isTaskAssigner(#id) or @authorize.isTaskManager(#id)")
    public void deleteTask(Long id) {
        Task task = getTaskByIdInternal(id);
        taskRepository.delete(task);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or @authorize.isTaskAssignee(#id) or @authorize.isTaskManager(#id)")
    public TaskResponse getTaskById(Long id) {
        Task task = getTaskByIdInternal(id);
        return populateFiles(taskMapper.toTaskResponse(task));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<TaskResponse> getAllTasks() {
        List<Task> tasks = taskRepository.findAll();
        List<TaskResponse> taskResponses = taskMapper.toTaskResponseList(tasks);
        // Filter tasks based on user access
        taskResponses = filterAccessibleTasks(taskResponses);
        return populateFilesList(taskResponses);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<TaskResponse> getTasksByProjectId(Long projectId) {
        List<Task> tasks = taskRepository.findByTargetTypeAndTargetId(ContentTargetType.PROJECT, projectId);
        return populateFilesList(taskMapper.toTaskResponseList(tasks));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<TaskResponse> getTasksByDepartmentId(Long departmentId) {
        List<Task> tasks = taskRepository.findByTargetTypeAndTargetId(ContentTargetType.PROJECT, departmentId);
        return populateFilesList(taskMapper.toTaskResponseList(tasks));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<TaskResponse> getMyTaskCreated() {
        Long userId = getUserId();
        List<Task> tasks = taskRepository.findByAssignerId(userId);
        return populateFilesList(taskMapper.toTaskResponseList(tasks));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<TaskResponse> getMyTask() {
        Long userId = getUserId();
        List<Task> tasks = taskRepository.findByAssigneeId(userId);
        return populateFilesList(taskMapper.toTaskResponseList(tasks));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<TaskResponse> getMyTaskByProjectId(Long projectId) {
        Long userId = getUserId();
        List<Task> tasks = taskRepository.findByAssigneeIdAndTargetTypeAndTargetId(userId, ContentTargetType.PROJECT, projectId);
        return populateFilesList(taskMapper.toTaskResponseList(tasks));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<TaskResponse> getTasksByStatus(TaskStatus status) {
        List<Task> tasks = taskRepository.findByStatus(status);
        return populateFilesList(taskMapper.toTaskResponseList(tasks));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<TaskResponse> getMyTasksByStatus(TaskStatus status) {
        Long userId = getUserId();
        List<Task> tasks = taskRepository.findByAssigneeIdAndStatus(userId, status);
        return populateFilesList(taskMapper.toTaskResponseList(tasks));
    }

    // Helper methods
    private Task getTaskByIdInternal(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Task not found"));
    }

    private Long getUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userPrincipal.getId();
    }

    // For schedule job
    public List<Task> getTasksDueToday() {
        return taskRepository.findTasksDueToday();
    }

    public List<Task> getOverdueTasks() {
        return taskRepository.findOverdueTasks();
    }

    // Populate files for TaskResponse, similar to ContentService
    private TaskResponse populateFiles(TaskResponse taskResponse) {
        if (taskResponse == null) return null;
        try {
            if (taskResponse.getHasFiles() != null && taskResponse.getHasFiles()) {
                // Only call fileService if hasFiles is true
                List<FileResponse> files = fileService.getFilesByEntity(FileTargetType.TASK, taskResponse.getId());
                taskResponse.setFiles(files);
                
                if (files.isEmpty()) {
                    // hasFiles is true but no files found, update database
                    Task task = taskRepository.findById(taskResponse.getId()).orElse(null);
                    if (task != null) {
                        task.setHasFiles(false);
                        taskRepository.save(task);
                    }
                    taskResponse.setHasFiles(false);
                } else {
                    taskResponse.setHasFiles(true);
                }
            } else {
                // hasFiles is false or task not found, don't call fileService
                taskResponse.setFiles(List.of());
                taskResponse.setHasFiles(false);
            }
        } catch (Exception e) {
            taskResponse.setFiles(List.of());
            taskResponse.setHasFiles(false);
        }
        
        return taskResponse;
    }

    private List<TaskResponse> populateFilesList(List<TaskResponse> taskResponses) {
        return taskResponses.stream()
                .map(this::populateFiles)
                .toList();
    }

    private List<TaskResponse> filterAccessibleTasks(List<TaskResponse> tasks) {
        Long userId = getUserId();
        return tasks.stream()
                .filter(task -> authorizationService.canAccessTask(task.getId()))
                .toList();
    }
}
