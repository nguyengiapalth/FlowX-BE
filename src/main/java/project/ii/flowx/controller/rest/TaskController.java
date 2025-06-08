package project.ii.flowx.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import project.ii.flowx.applications.service.communicate.TaskService;
import project.ii.flowx.model.dto.FlowXResponse;
import project.ii.flowx.model.dto.task.TaskCreateRequest;
import project.ii.flowx.model.dto.task.TaskResponse;
import project.ii.flowx.model.dto.task.TaskUpdateRequest;
import project.ii.flowx.shared.enums.TaskStatus;

import java.util.List;

@RestController
@RequestMapping("api/task")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Task", description = "Task API")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {
    TaskService taskService;

    @Operation(
            summary = "Create a new task",
            description = "Creates a new task in the system. " +
                    "The task details are provided in the request body.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Task created successfully"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid task details provided"
                    )
            }
    )
    @PostMapping
    public FlowXResponse<TaskResponse> createTask(@Valid @RequestBody TaskCreateRequest taskCreateRequest) {
        log.info("Creating task with details: {}", taskCreateRequest);
        return FlowXResponse.<TaskResponse>builder()
                .data(taskService.createTask(taskCreateRequest))
                .message("Task created successfully")
                .code(201)
                .build();
    }

    @Operation(
            summary = "Update task",
            description = "Updates the details of an existing task.",
            parameters = {
                    @Parameter(name = "id", description = "ID of the task to be updated")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Task updated successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Task not found"
                    )
            }
    )
    @PutMapping("/{id}")
    public FlowXResponse<TaskResponse> updateTask(@PathVariable Long id, @Valid @RequestBody TaskUpdateRequest taskUpdateRequest) {
        log.info("Updating task ID: {}", id);
        return FlowXResponse.<TaskResponse>builder()
                .data(taskService.updateTask(id, taskUpdateRequest))
                .message("Task updated successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Update task status",
            description = "Updates the status of a specific task.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Task status updated successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Task not found"
                    )
            }
    )
    @PutMapping("/{id}/status")
    public FlowXResponse<TaskResponse> updateTaskStatus(@PathVariable Long id, @RequestParam TaskStatus status) {
        log.info("Updating task status - ID: {}, Status: {}", id, status);
        return FlowXResponse.<TaskResponse>builder()
                .data(taskService.updateTaskStatus(id, status))
                .message("Task status updated successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Mark task as completed",
            description = "Marks a specific task as completed.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Task marked as completed successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Task not found"
                    )
            }
    )
    @PutMapping("/{id}/complete")
    public FlowXResponse<TaskResponse> markTaskCompleted(@PathVariable Long id) {
        log.info("Marking task as completed - ID: {}", id);
        return FlowXResponse.<TaskResponse>builder()
                .data(taskService.markTaskCompleted(id))
                .message("Task marked as completed successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Mark task as incomplete",
            description = "Marks a specific task as incomplete.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Task marked as incomplete successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Task not found"
                    )
            }
    )
    @PutMapping("/{id}/incomplete")
    public FlowXResponse<TaskResponse> markTaskIncomplete(@PathVariable Long id) {
        log.info("Marking task as incomplete - ID: {}", id);
        return FlowXResponse.<TaskResponse>builder()
                .data(taskService.markTaskIncomplete(id))
                .message("Task marked as incomplete successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Delete task",
            description = "Deletes a task by its unique ID.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Task deleted successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Task not found"
                    )
            }
    )
    @DeleteMapping("/{id}")
    public FlowXResponse<Void> deleteTask(@PathVariable Long id) {
        log.info("Deleting task ID: {}", id);
        taskService.deleteTask(id);
        return FlowXResponse.<Void>builder()
                .message("Task deleted successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Get task by ID",
            description = "Retrieves a task by its unique ID.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Task retrieved successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Task not found"
                    )
            }
    )
    @GetMapping("/{id}")
    public FlowXResponse<TaskResponse> getTaskById(@PathVariable Long id) {
        log.info("Fetching task by ID: {}", id);
        return FlowXResponse.<TaskResponse>builder()
                .data(taskService.getTaskById(id))
                .message("Task retrieved successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Get all tasks",
            description = "Retrieves a list of all tasks in the system.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of tasks retrieved successfully"
                    )
            }
    )
    @GetMapping
    public FlowXResponse<List<TaskResponse>> getAllTasks() {
        log.info("Fetching all tasks");
        return FlowXResponse.<List<TaskResponse>>builder()
                .data(taskService.getAllTasks())
                .message("List of tasks retrieved successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Get tasks by project",
            description = "Retrieves all tasks associated with a specific project.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Tasks retrieved successfully"
                    )
            }
    )
    @GetMapping("/project/{projectId}")
    public FlowXResponse<List<TaskResponse>> getTasksByProjectId(@PathVariable Long projectId) {
        log.info("Fetching tasks by project ID: {}", projectId);
        return FlowXResponse.<List<TaskResponse>>builder()
                .data(taskService.getTasksByProjectId(projectId))
                .message("Tasks retrieved successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Get tasks by department",
            description = "Retrieves all tasks associated with a specific department.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Tasks retrieved successfully"
                    )
            }
    )
    @GetMapping("/department/{departmentId}")
    public FlowXResponse<List<TaskResponse>> getTasksByDepartmentId(@PathVariable Long departmentId) {
        log.info("Fetching tasks by department ID: {}", departmentId);
        return FlowXResponse.<List<TaskResponse>>builder()
                .data(taskService.getTasksByDepartmentId(departmentId))
                .message("Tasks retrieved successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Get tasks by status",
            description = "Retrieves all tasks with a specific status.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Tasks retrieved successfully"
                    )
            }
    )
    @GetMapping("/status/{status}")
    public FlowXResponse<List<TaskResponse>> getTasksByStatus(@PathVariable TaskStatus status) {
        log.info("Fetching tasks by status: {}", status);
        return FlowXResponse.<List<TaskResponse>>builder()
                .data(taskService.getTasksByStatus(status))
                .message("Tasks retrieved successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Get my created tasks",
            description = "Retrieves all tasks created by the current user.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "My created tasks retrieved successfully"
                    )
            }
    )
    @GetMapping("/my-created")
    public FlowXResponse<List<TaskResponse>> getMyTaskCreated() {
        log.info("Fetching tasks created by current user");
        return FlowXResponse.<List<TaskResponse>>builder()
                .data(taskService.getMyTaskCreated())
                .message("My created tasks retrieved successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Get my assigned tasks",
            description = "Retrieves all tasks assigned to the current user.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "My assigned tasks retrieved successfully"
                    )
            }
    )
    @GetMapping("/my-assigned")
    public FlowXResponse<List<TaskResponse>> getMyTask() {
        log.info("Fetching tasks assigned to current user");
        return FlowXResponse.<List<TaskResponse>>builder()
                .data(taskService.getMyTask())
                .message("My assigned tasks retrieved successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Get my tasks by project",
            description = "Retrieves tasks assigned to the current user within a specific project.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "My tasks retrieved successfully"
                    )
            }
    )
    @GetMapping("/my-assigned/project/{projectId}")
    public FlowXResponse<List<TaskResponse>> getMyTaskByProjectId(@PathVariable Long projectId) {
        log.info("Fetching my tasks by project ID: {}", projectId);
        return FlowXResponse.<List<TaskResponse>>builder()
                .data(taskService.getMyTaskByProjectId(projectId))
                .message("My tasks retrieved successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Get my tasks by status",
            description = "Retrieves tasks assigned to the current user with a specific status.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "My tasks retrieved successfully"
                    )
            }
    )
    @GetMapping("/my-assigned/status/{status}")
    public FlowXResponse<List<TaskResponse>> getMyTasksByStatus(@PathVariable TaskStatus status) {
        log.info("Fetching my tasks by status: {}", status);
        return FlowXResponse.<List<TaskResponse>>builder()
                .data(taskService.getMyTasksByStatus(status))
                .message("My tasks retrieved successfully")
                .code(200)
                .build();
    }
}
