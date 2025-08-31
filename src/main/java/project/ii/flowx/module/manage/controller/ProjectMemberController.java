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
import project.ii.flowx.module.manage.service.ProjectMemberService;
import project.ii.flowx.module.manage.dto.projectmember.ProjectMemberCreateRequest;
import project.ii.flowx.module.manage.dto.projectmember.ProjectMemberResponse;
import project.ii.flowx.applications.enums.RoleDefault;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/project-member")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Project ConversationMember", description = "Project ConversationMember API")
@SecurityRequirement(name = "bearerAuth")
public class ProjectMemberController {
    ProjectMemberService projectMemberService;

    @Operation(
            summary = "Create a new project member",
            description = "Creates a new project member in the system. " +
                    "The project member details are provided in the request body.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Project member created successfully"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid project member details provided"
                    )
            }
    )
    @PostMapping("/add")
    public Response<ProjectMemberResponse> createProjectMember(@RequestBody ProjectMemberCreateRequest request) {
        log.info("Creating project member with details: {}", request);
        return Response.<ProjectMemberResponse>builder()
                .data(projectMemberService.createProjectMember(request))
                .message("Project member created successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Update project member",
            description = "Updates the details of an existing project member.",
            parameters = {
                    @Parameter(name = "id", description = "ID of the project member to be updated")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Project member updated successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Project member not found"
                    )
            }
    )
    @PutMapping("/{id}/role")
    public Response<ProjectMemberResponse> updateMemberRole(
            @PathVariable UUID id,
            @RequestBody RoleDefault role) {
        return Response.<ProjectMemberResponse>builder()
                .data(projectMemberService.updateMemberRole(id, role))
                .message("Project member updated successfully")
                .code(200)
                .build();
    }


    @Operation(
            summary = "Delete project member",
            description = "Deletes a project member by its unique ID.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Project member deleted successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Project member not found"
                    )
            }
    )
    @DeleteMapping("/delete/{id}")
    public Response<Void> deleteMember(@PathVariable UUID id) {
        projectMemberService.deleteProjectMember(id);
        return Response.<Void>builder()
                .message("Project member deleted successfully")
                .code(200)
                .build();
    }


    @Operation(
            summary = "Get all project members by project ID",
            description = "Retrieves a list of all project members in the system.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of project members retrieved successfully"
                    )
            }
    )
    @GetMapping("/get-by-project/{projectId}")
    public Response<List<ProjectMemberResponse>> getMembersByProjectId(@PathVariable UUID projectId) {
        log.info("Fetching all project members for project ID: {}", projectId);
        return Response.<List<ProjectMemberResponse>>builder()
                .data(projectMemberService.getByProject(projectId))
                .message("List of project members retrieved successfully")
                .code(200)
                .build();
    }
}