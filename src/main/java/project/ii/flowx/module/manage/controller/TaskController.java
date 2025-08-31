package project.ii.flowx.module.manage.controller;

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
import project.ii.flowx.dto.Response;
import project.ii.flowx.module.manage.service.TaskService;
import project.ii.flowx.module.manage.dto.task.TaskCreateRequest;
import project.ii.flowx.module.manage.dto.task.TaskResponse;
import project.ii.flowx.module.manage.dto.task.TaskUpdateRequest;
import project.ii.flowx.applications.enums.TaskStatus;

import java.util.List;
import java.util.UUID;

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
    public Response<TaskResponse> createTask(@Valid @RequestBody TaskCreateRequest taskCreateRequest) {
        log.info("Creating task with details: {}", taskCreateRequest);
        return Response.<TaskResponse>builder()
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
    public Response<TaskResponse> updateTask(@PathVariable UUID id, @Valid @RequestBody TaskUpdateRequest taskUpdateRequest) {
        log.info("Updating task ID: {}", id);
        return Response.<TaskResponse>builder()
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
    public Response<TaskResponse> updateTaskStatus(@PathVariable UUID id, @RequestParam TaskStatus status) {
        log.info("Updating task status - ID: {}, Status: {}", id, status);
        return Response.<TaskResponse>builder()
                .data(taskService.updateTaskStatus(id, status))
                .message("Task status updated successfully")
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
    public Response<Void> deleteTask(@PathVariable UUID id) {
        log.info("Deleting task ID: {}", id);
        taskService.deleteTask(id);
        return Response.<Void>builder()
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
    public Response<TaskResponse> getTaskById(@PathVariable UUID id) {
        log.info("Fetching task by ID: {}", id);
        return Response.<TaskResponse>builder()
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
    public Response<List<TaskResponse>> getAllTasks() {
        log.info("Fetching all tasks");
        return Response.<List<TaskResponse>>builder()
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
    public Response<List<TaskResponse>> getTasksByProjectId(@PathVariable UUID projectId) {
        log.info("Fetching tasks by project ID: {}", projectId);
        return Response.<List<TaskResponse>>builder()
                .data(taskService.getTasksByProjectId(projectId))
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
    public Response<List<TaskResponse>> getTasksByStatus(@PathVariable TaskStatus status) {
        log.info("Fetching tasks by status: {}", status);
        return Response.<List<TaskResponse>>builder()
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
    public Response<List<TaskResponse>> getMyTaskCreated() {
        log.info("Fetching tasks created by current user");
        return Response.<List<TaskResponse>>builder()
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
    public Response<List<TaskResponse>> getMyTask() {
        log.info("Fetching tasks assigned to current user");
        return Response.<List<TaskResponse>>builder()
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
    public Response<List<TaskResponse>> getMyTaskByProjectId(@PathVariable UUID projectId) {
        log.info("Fetching my tasks by project ID: {}", projectId);
        return Response.<List<TaskResponse>>builder()
                .data(taskService.getMyTaskByProjectId(projectId))
                .message("My tasks retrieved successfully")
                .code(200)
                .build();
    }

}
