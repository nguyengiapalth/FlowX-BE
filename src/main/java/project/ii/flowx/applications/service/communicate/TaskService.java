package project.ii.flowx.applications.service.communicate;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ii.flowx.model.entity.Task;
import project.ii.flowx.model.repository.TaskRepository;
import project.ii.flowx.model.dto.task.TaskCreateRequest;
import project.ii.flowx.model.dto.task.TaskResponse;
import project.ii.flowx.model.dto.task.TaskUpdateRequest;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;
import project.ii.flowx.model.mapper.TaskMapper;
import project.ii.flowx.security.UserPrincipal;
import project.ii.flowx.shared.enums.ContentTargetType;
import project.ii.flowx.shared.enums.TaskStatus;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TaskService {
    TaskRepository taskRepository;
    TaskMapper taskMapper;

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or @authorize.hasProjectRole('MANAGER', #taskCreateRequest.projectId)")
    public TaskResponse createTask(TaskCreateRequest taskCreateRequest) {
        log.info("Creating new task");
        log.debug("Task create request: {}", taskCreateRequest);

        Task task = taskMapper.toTask(taskCreateRequest);
        task.setIsCompleted(false);
        if (task.getStatus() == null) task.setStatus(TaskStatus.TO_DO);
        if (task.getHasFiles() == null) task.setHasFiles(false);
        
        task = taskRepository.save(task);
        log.info("Successfully created task with ID: {}", task.getId());

        return taskMapper.toTaskResponse(task);
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or @authorize.hasProjectRole('MANAGER', #taskUpdateRequest.projectId) or authentication.principal.id == #taskUpdateRequest.assigneeId")
    public TaskResponse updateTask(Long id, TaskUpdateRequest taskUpdateRequest) {
        log.info("Updating task ID: {}", id);
        log.debug("Task update request: {}", taskUpdateRequest);
        
        Task task = getTaskByIdInternal(id);
        taskMapper.updateTaskFromRequest(task, taskUpdateRequest);
        task = taskRepository.save(task);
        
        log.info("Successfully updated task ID: {}", id);
        return taskMapper.toTaskResponse(task);
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or @authorize.hasProjectRole('MANAGER', #taskUpdateRequest.projectId) or authentication.principal.id == #taskUpdateRequest.assigneeId")
    public TaskResponse updateTaskStatus(Long id, TaskStatus status) {
        log.info("Updating task status - ID: {}, New Status: {}", id, status);

        Task task = getTaskByIdInternal(id);
        TaskStatus oldStatus = task.getStatus();
        task.setStatus(status);

        // Auto-complete task when status is DONE
        if (status == TaskStatus.COMPLETED) {
            task.setIsCompleted(true);
            log.debug("Auto-marking task {} as completed due to DONE status", id);
        }

        task = taskRepository.save(task);
        log.info("Successfully updated task status from {} to {} for task ID: {}", oldStatus, status, id);

        return taskMapper.toTaskResponse(task);
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_MANAGER') || @authorize.hasProjectRole('MANAGER', #id)")
    public void deleteTask(Long id) {
        log.info("Deleting task ID: {}", id);

        Task task = getTaskByIdInternal(id);
        taskRepository.delete(task);

        log.info("Successfully deleted task ID: {}", id);
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id) {
        log.debug("Fetching task by ID: {}", id);

        Task task = getTaskByIdInternal(id);
        return taskMapper.toTaskResponse(task);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER')")
    public List<TaskResponse> getAllTasks() {
        log.info("Fetching all tasks");

        List<Task> tasks = taskRepository.findAll();
        List<TaskResponse> responses = taskMapper.toTaskResponseList(tasks);

        log.info("Successfully fetched {} tasks", responses.size());
        return responses;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER') || @authorize.hasProjectRole('MANAGER', projectId)")
    public List<TaskResponse> getTasksByProjectId(Long projectId) {
        log.info("Fetching tasks by project ID: {}", projectId);

        List<Task> tasks = taskRepository.findByTargetTypeAndTargetId(ContentTargetType.PROJECT, projectId);
        List<TaskResponse> responses = taskMapper.toTaskResponseList(tasks);

        log.info("Successfully fetched {} tasks for project ID: {}", responses.size(), projectId);
        return responses;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER') || @authorize.hasDepartmentRole('MANAGER', #departmentId)")
    public List<TaskResponse> getTasksByDepartmentId(Long departmentId) {
        log.info("Fetching tasks by department ID: {}", departmentId);

        List<Task> tasks = taskRepository.findByTargetTypeAndTargetId(ContentTargetType.PROJECT, departmentId);
        List<TaskResponse> responses = taskMapper.toTaskResponseList(tasks);

        log.info("Successfully fetched {} tasks for department ID: {}", responses.size(), departmentId);
        return responses;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<TaskResponse> getMyTaskCreated() {
        Long userId = getUserId();
        log.info("Fetching tasks created by user: {}", userId);

        List<Task> tasks = taskRepository.findByAssignerId(userId);
        List<TaskResponse> responses = taskMapper.toTaskResponseList(tasks);

        log.info("Successfully fetched {} tasks created by user: {}", responses.size(), userId);
        return responses;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<TaskResponse> getMyTask() {
        Long userId = getUserId();
        log.info("Fetching tasks assigned to user: {}", userId);

        List<Task> tasks = taskRepository.findByAssigneeId(userId);
        List<TaskResponse> responses = taskMapper.toTaskResponseList(tasks);

        log.info("Successfully fetched {} tasks assigned to user: {}", responses.size(), userId);
        return responses;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<TaskResponse> getMyTaskByProjectId(Long projectId) {
        Long userId = getUserId();
        log.info("Fetching my tasks by project ID: {} for user: {}", projectId, userId);

        List<Task> tasks = taskRepository.findByAssigneeIdAndTargetTypeAndTargetId(userId, ContentTargetType.PROJECT, projectId);
        List<TaskResponse> responses = taskMapper.toTaskResponseList(tasks);

        log.info("Successfully fetched {} my tasks for project ID: {} by user: {}", responses.size(), projectId, userId);
        return responses;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public List<TaskResponse> getTasksByStatus(TaskStatus status) {
        log.info("Fetching tasks by status: {}", status);

        List<Task> tasks = taskRepository.findByStatus(status);
        List<TaskResponse> responses = taskMapper.toTaskResponseList(tasks);

        log.info("Successfully fetched {} tasks with status: {}", responses.size(), status);
        return responses;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<TaskResponse> getMyTasksByStatus(TaskStatus status) {
        Long userId = getUserId();
        log.info("Fetching my tasks by status: {} for user: {}", status, userId);

        List<Task> tasks = taskRepository.findByAssigneeIdAndStatus(userId, status);
        List<TaskResponse> responses = taskMapper.toTaskResponseList(tasks);

        log.info("Successfully fetched {} my tasks with status: {} for user: {}", responses.size(), status, userId);
        return responses;
    }

    @Transactional
    @PreAuthorize("isAuthenticated()")
    public TaskResponse markTaskCompleted(Long id) {
        log.info("Marking task as completed - ID: {}", id);

        Task task = getTaskByIdInternal(id);
        task.setIsCompleted(true);
        task.setStatus(TaskStatus.COMPLETED);

        task = taskRepository.save(task);
        log.info("Successfully marked task ID: {} as completed", id);

        return taskMapper.toTaskResponse(task);
    }

    @Transactional
    @PreAuthorize("isAuthenticated()")
    public TaskResponse markTaskIncomplete(Long id) {
        log.info("Marking task as incomplete - ID: {}", id);

        Task task = getTaskByIdInternal(id);
        task.setIsCompleted(false);
        if (task.getStatus() == TaskStatus.COMPLETED) task.setStatus(TaskStatus.IN_PROGRESS);

        task = taskRepository.save(task);
        log.info("Successfully marked task ID: {} as incomplete", id);

        return taskMapper.toTaskResponse(task);
    }

    // Helper methods
    private Task getTaskByIdInternal(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Task not found with ID: {}", id);
                    return new FlowXException(FlowXError.NOT_FOUND, "Task not found");
                });
    }

    private Long getUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userPrincipal.getId();
    }
}
