package project.ii.flowx.controller.rest;

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
import project.ii.flowx.applications.service.manage.ProjectService;
import project.ii.flowx.model.dto.FlowXResponse;
import project.ii.flowx.model.dto.project.ProjectCreateRequest;
import project.ii.flowx.model.dto.project.ProjectResponse;
import project.ii.flowx.model.dto.project.ProjectUpdateRequest;
import project.ii.flowx.shared.enums.ProjectStatus;

import java.util.List;

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
    public FlowXResponse<ProjectResponse> createProject(@RequestBody ProjectCreateRequest projectCreateRequest) {
        log.info("Creating project with details: {}", projectCreateRequest);
        return FlowXResponse.<ProjectResponse>builder()
                .data(projectService.createProject(projectCreateRequest))
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
    public FlowXResponse<ProjectResponse> updateProject(
            @PathVariable Long id, 
            @RequestBody ProjectUpdateRequest projectUpdateRequest) {
        return FlowXResponse.<ProjectResponse>builder()
                .data(projectService.updateProject(id, projectUpdateRequest))
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
    public FlowXResponse<ProjectResponse> updateProjectStatus(
            @PathVariable Long id,
            @RequestParam ProjectStatus status) {
        return FlowXResponse.<ProjectResponse>builder()
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
    public FlowXResponse<ProjectResponse> completeProject(@PathVariable Long id) {
        return FlowXResponse.<ProjectResponse>builder()
                .data(projectService.completeProject(id))
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
    public FlowXResponse<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return FlowXResponse.<Void>builder()
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
    @GetMapping("/getall")
    public FlowXResponse<List<ProjectResponse>> getAllProjects() {
        return FlowXResponse.<List<ProjectResponse>>builder()
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
    public FlowXResponse<ProjectResponse> getProjectById(@PathVariable Long id) {
        return FlowXResponse.<ProjectResponse>builder()
                .data(projectService.getProjectById(id))
                .message("Project retrieved successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Get projects by status",
            description = "Retrieves a list of projects filtered by their status.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of projects retrieved successfully"
                    )
            }
    )
    @GetMapping("/get/status")
    public FlowXResponse<List<ProjectResponse>> getProjectsByStatus(@RequestParam ProjectStatus status) {
        return FlowXResponse.<List<ProjectResponse>>builder()
                .data(projectService.getProjectsByStatus(status))
                .message("List of projects retrieved successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Get projects by department ID",
            description = "Retrieves a list of projects filtered by their department ID.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of projects retrieved successfully"
                    )
            }
    )
    @GetMapping("/get/department/{departmentId}")
    public FlowXResponse<List<ProjectResponse>> getProjectsByDepartmentId(@PathVariable long departmentId) {
        return FlowXResponse.<List<ProjectResponse>>builder()
                .data(projectService.getProjectsByDepartmentId(departmentId))
                .message("List of projects retrieved successfully")
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
    public FlowXResponse<List<ProjectResponse>> getMyProjects() {
        log.info("Fetching projects assigned to the current user");
        return FlowXResponse.<List<ProjectResponse>>builder()
                .data(projectService.getMyProjects())
                .message("List of my projects retrieved successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Update project background",
            description = "Updates the background image of a project.",
            parameters = {
                    @Parameter(name = "id", description = "ID of the project to be updated")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Project background updated successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Project not found"
                    )
            }
    )
    @PutMapping("/update-background/{id}")
    public FlowXResponse<ProjectResponse> updateProjectBackground(@PathVariable Long id, @RequestBody String background) {

        return FlowXResponse.<ProjectResponse>builder()
                .data(projectService.updateProjectBackground(id, background))
                .message("Project background updated successfully")
                .code(200)
                .build();
    }
}
