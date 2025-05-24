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
import project.ii.flowx.model.dto.projectmember.ProjectMemberUpdateRequest;
import project.ii.flowx.shared.enums.MemberStatus;

import java.util.List;

@RestController
@RequestMapping("api/project-member")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
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
    @PutMapping("/update/{id}")
    public FlowXResponse<ProjectMemberResponse> updateProjectMember(
            @PathVariable Long id, 
            @RequestBody ProjectMemberUpdateRequest projectMemberUpdateRequest) {
        return FlowXResponse.<ProjectMemberResponse>builder()
                .data(projectMemberService.updateProjectMember(id, projectMemberUpdateRequest))
                .message("Project member updated successfully")
                .code(200)
                .build();
    }

    // Bulk update status
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
    public FlowXResponse<Void> deleteProjectMember(@PathVariable Long id) {
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
    public FlowXResponse<ProjectMemberResponse> getProjectMemberById(@PathVariable Long id) {
        return FlowXResponse.<ProjectMemberResponse>builder()
                .data(projectMemberService.getProjectMemberById(id))
                .message("Project member retrieved successfully")
                .code(200)
                .build();
    }

    // Lấy danh sách members theo project ID
    @Operation(
            summary = "Get members by project ID",
            description = "Retrieves all members of a specific project.",
            parameters = {
                    @Parameter(name = "projectId", description = "ID of the project")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Project members retrieved successfully"
                    )
            }
    )
    @GetMapping("/project/{projectId}")
    public FlowXResponse<List<ProjectMemberResponse>> getMembersByProjectId(
            @PathVariable Long projectId) {
        log.info("Getting members for project ID: {}", projectId);
        return FlowXResponse.<List<ProjectMemberResponse>>builder()
                .data(projectMemberService.getByProjectId(projectId))
                .message("Project members retrieved successfully")
                .code(200)
                .build();
    }

    // Lấy danh sách projects theo user ID
    @Operation(
            summary = "Get projects by user ID",
            description = "Retrieves all projects that a user is a member of.",
            parameters = {
                    @Parameter(name = "userId", description = "ID of the user")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User projects retrieved successfully"
                    )
            }
    )
    @GetMapping("/user/{userId}")
    public FlowXResponse<List<ProjectMemberResponse>> getProjectsByUserId(
            @PathVariable Long userId) {
        log.info("Getting projects for user ID: {}", userId);
        return FlowXResponse.<List<ProjectMemberResponse>>builder()
                .data(projectMemberService.getByUserId(userId))
                .message("User projects retrieved successfully")
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

    // Lấy active members của project
    @Operation(
            summary = "Get active members by project ID",
            description = "Retrieves all active members of a specific project.",
            parameters = {
                    @Parameter(name = "projectId", description = "ID of the project")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Active project members retrieved successfully"
                    )
            }
    )
    @GetMapping("/project/{projectId}/active")
    public FlowXResponse<List<ProjectMemberResponse>> getActiveMembers(
            @PathVariable Long projectId) {
        log.info("Getting active members for project ID: {}", projectId);
        return FlowXResponse.<List<ProjectMemberResponse>>builder()
                .data(projectMemberService.getActiveMembers(projectId))
                .message("Active project members retrieved successfully")
                .code(200)
                .build();
    }


//    // Cache management
//    @Operation(
//            summary = "Clear all cache",
//            description = "Clears all cached data for project members. Use with caution.",
//            responses = {
//                    @ApiResponse(
//                            responseCode = "200",
//                            description = "Cache cleared successfully"
//                    )
//            }
//    )
//    @PostMapping("/cache/clear")
//    public FlowXResponse<Void> clearCache() {
//        log.info("Clearing all project member cache");
//        projectMemberService.clearAllCache();
//        return FlowXResponse.<Void>builder()
//                .message("Cache cleared successfully")
//                .code(200)
//                .build();
//    }
//
//    @Operation(
//            summary = "Clear project members cache",
//            description = "Clears cached data for a specific project's members.",
//            parameters = {
//                    @Parameter(name = "projectId", description = "ID of the project")
//            },
//            responses = {
//                    @ApiResponse(
//                            responseCode = "200",
//                            description = "Project cache cleared successfully"
//                    )
//            }
//    )
//    @PostMapping("/cache/clear/project/{projectId}")
//    public FlowXResponse<Void> clearProjectCache(@PathVariable Long projectId) {
//        log.info("Clearing cache for project ID: {}", projectId);
//        projectMemberService.clearProjectMembersCache(projectId);
//        return FlowXResponse.<Void>builder()
//                .message("Project cache cleared successfully")
//                .code(200)
//                .build();
//    }
//
//    @Operation(
//            summary = "Clear user projects cache",
//            description = "Clears cached data for a specific user's projects.",
//            parameters = {
//                    @Parameter(name = "userId", description = "ID of the user")
//            },
//            responses = {
//                    @ApiResponse(
//                            responseCode = "200",
//                            description = "User cache cleared successfully"
//                    )
//            }
//    )
//    @PostMapping("/cache/clear/user/{userId}")
//    public FlowXResponse<Void> clearUserCache(@PathVariable Long userId) {
//        log.info("Clearing cache for user ID: {}", userId);
//        projectMemberService.clearUserProjectsCache(userId);
//        return FlowXResponse.<Void>builder()
//                .message("User cache cleared successfully")
//                .code(200)
//                .build();
//    }
}