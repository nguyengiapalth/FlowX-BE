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
import project.ii.flowx.applications.service.manage.ProjectMemberService;
import project.ii.flowx.model.dto.FlowXResponse;
import project.ii.flowx.model.dto.projectmember.ProjectMemberCreateRequest;
import project.ii.flowx.model.dto.projectmember.ProjectMemberResponse;
import project.ii.flowx.shared.enums.MemberStatus;
import project.ii.flowx.shared.enums.RoleDefault;

import java.util.List;

@RestController
@RequestMapping("api/project-member")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Project Member", description = "Project Member API")
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
    public FlowXResponse<ProjectMemberResponse> createProjectMember(
            @RequestBody ProjectMemberCreateRequest projectMemberCreateRequest) {
        log.info("Creating project member with details: {}", projectMemberCreateRequest);
        return FlowXResponse.<ProjectMemberResponse>builder()
                .data(projectMemberService.createProjectMember(projectMemberCreateRequest))
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
    public FlowXResponse<ProjectMemberResponse> updateMemberRole(
            @PathVariable Long id,
            @RequestBody RoleDefault role) {
        return FlowXResponse.<ProjectMemberResponse>builder()
                .data(projectMemberService.updateMemberRole(id, role))
                .message("Project member updated successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Update member status",
            description = "Updates the status of a project member.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Member status updated successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Project member not found"
                    )
            }
    )
    @PutMapping("/{id}/status")
    public FlowXResponse<ProjectMemberResponse> updateMemberStatus(
            @PathVariable Long id,
            @RequestParam MemberStatus status) {
        return FlowXResponse.<ProjectMemberResponse>builder()
                .data(projectMemberService.updateMemberStatus(id, status))
                .message("Member status updated successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Bulk update member status",
            description = "Updates the status of multiple project members at once.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Member statuses updated successfully"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid member IDs or status"
                    )
            }
    )
    @PutMapping("/bulk-status")
    public FlowXResponse<Void> bulkUpdateMemberStatus(
            @RequestParam List<Long> memberIds,
            @RequestParam MemberStatus status) {
        log.info("Bulk updating status for {} members to: {}", memberIds.size(), status);
        projectMemberService.bulkUpdateMemberStatus(memberIds, status);
        return FlowXResponse.<Void>builder()
                .message("Member statuses updated successfully")
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
    public FlowXResponse<Void> deleteMember(@PathVariable Long id) {
        projectMemberService.deleteProjectMember(id);
        return FlowXResponse.<Void>builder()
                .message("Project member deleted successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Get project member by ID",
            description = "Retrieves a project member by its unique ID.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Project member retrieved successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Project member not found"
                    )
            }
    )
    @GetMapping("/get/{id}")
    public FlowXResponse<ProjectMemberResponse> getPMemberById(@PathVariable Long id) {
        return FlowXResponse.<ProjectMemberResponse>builder()
                .data(projectMemberService.getProjectMemberById(id))
                .message("Project member retrieved successfully")
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
    public FlowXResponse<List<ProjectMemberResponse>> getMembersByProjectId(@PathVariable Long projectId) {
        log.info("Fetching all project members for project ID: {}", projectId);
        return FlowXResponse.<List<ProjectMemberResponse>>builder()
                .data(projectMemberService.getByProject(projectId))
                .message("List of project members retrieved successfully")
                .code(200)
                .build();
    }

}