package project.ii.flowx.module.manage.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import project.ii.flowx.dto.Response;
import project.ii.flowx.module.manage.service.ProjectService;
import project.ii.flowx.module.manage.dto.project.ProjectCreateRequest;
import project.ii.flowx.module.manage.dto.project.ProjectResponse;
import project.ii.flowx.module.manage.dto.project.ProjectUpdateRequest;
import project.ii.flowx.applications.enums.ProjectStatus;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/project")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Project", description = "Project API")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {
    ProjectService projectService;

    @Operation(
            summary = "Create a new project",
            description = "Creates a new project in the system. " +
                    "The project details are provided in the request body.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Project created successfully"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid project details provided"
                    )
            }
    )
    @PostMapping("/create")
    public Response<ProjectResponse> create(@RequestBody ProjectCreateRequest request) {
        log.info("Creating project with details: {}", request);
        return Response.<ProjectResponse>builder()
                .data(projectService.createProject(request))
                .message("Project created successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Update project",
            description = "Updates the details of an existing project.",
            parameters = {
                    @Parameter(name = "id", description = "ID of the project to be updated")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Project updated successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Project not found"
                    )
            }
    )
    @PutMapping("/update/{id}")
    public Response<ProjectResponse> update(@PathVariable UUID id, @RequestBody ProjectUpdateRequest request) {
        return Response.<ProjectResponse>builder()
                .data(projectService.updateProject(id, request))
                .message("Project updated successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Update project status",
            description = "Updates the status of a specific project.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Project status updated successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Project not found"
                    )
            }
    )
    @PutMapping("/{id}/status")
    public Response<ProjectResponse> updateProjectStatus(
            @PathVariable UUID id,
            @RequestParam ProjectStatus status) {
        return Response.<ProjectResponse>builder()
                .data(projectService.updateProjectStatus(id, status))
                .message("Project status updated successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Complete project",
            description = "Marks a project as completed and sets the end date to today.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Project completed successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Project not found"
                    )
            }
    )
    @PutMapping("/{id}/complete")
    public Response<ProjectResponse> completeProject(@PathVariable UUID id) {
        return Response.<ProjectResponse>builder()
                .data(projectService.updateProjectStatus(id, ProjectStatus.COMPLETED))
                .message("Project completed successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Delete project",
            description = "Deletes a project by its unique ID.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Project deleted successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Project not found"
                    )
            }
    )
    @DeleteMapping("/delete/{id}")
    public Response<Void> deleteProject(@PathVariable UUID id) {
        projectService.deleteProject(id);
        return Response.<Void>builder()
                .message("Project deleted successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Get all projects",
            description = "Retrieves a list of all projects in the system.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of projects retrieved successfully"
                    )
            }
    )
    @GetMapping("/get-all")
    public Response<List<ProjectResponse>> getAllProjects() {
        return Response.<List<ProjectResponse>>builder()
                .data(projectService.getAllProjects())
                .message("List of projects retrieved successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Get project by ID",
            description = "Retrieves a project by its unique ID.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Project retrieved successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Project not found"
                    )
            }
    )
    @GetMapping("/get/{id}")
    public Response<ProjectResponse> getProjectById(@PathVariable UUID id) {
        return Response.<ProjectResponse>builder()
                .data(projectService.getProjectById(id))
                .message("Project retrieved successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Get my projects",
            description = "Retrieves a list of all projects assigned to the current user.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of my projects retrieved successfully"
                    )
            }
    )
    @GetMapping("/my-projects")
    public Response<List<ProjectResponse>> getMyProjects() {
        log.info("Fetching projects assigned to the current user");
        return Response.<List<ProjectResponse>>builder()
                .data(projectService.getMyProjects())
                .message("List of my projects retrieved successfully")
                .code(200)
                .build();
    }
}
