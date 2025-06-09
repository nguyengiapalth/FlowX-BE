package project.ii.flowx.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import project.ii.flowx.applications.service.auth.UserRoleService;
import project.ii.flowx.model.dto.FlowXResponse;
import project.ii.flowx.model.dto.userrole.UserRoleCreateRequest;
import project.ii.flowx.model.dto.userrole.UserRoleResponse;

import java.util.List;

@RestController
@RequestMapping("api/user-role")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "User Role", description = "User Role API")
//@SecurityRequirement(name = "bearerAuth")
public class UserRoleController {
    UserRoleService userRoleService;

    @Operation(
            summary = "Get roles for user",
            description = "Retrieves all roles assigned to a specific user.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Roles retrieved successfully"
                    )
            }
    )
    @GetMapping("/user/{userId}")
    public FlowXResponse<List<UserRoleResponse>> getRolesForUser(@PathVariable Long userId) {
        return FlowXResponse.<List<UserRoleResponse>>builder()
                .data(userRoleService.getRolesForUser(userId))
                .message("User roles retrieved successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Get users for role",
            description = "Retrieves all users assigned to a specific role.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Users retrieved successfully"
                    )
            }
    )
    @GetMapping("/role/{roleId}")
    public FlowXResponse<List<UserRoleResponse>> getUsersForRole(@PathVariable Long roleId) {
        return FlowXResponse.<List<UserRoleResponse>>builder()
                .data(userRoleService.getUsersForRole(roleId))
                .message("Users for role retrieved successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Get global roles for user",
            description = "Retrieves all global roles assigned to a specific user.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Global roles retrieved successfully"
                    )
            }
    )
    @GetMapping("/global/{userId}")
    public FlowXResponse<List<UserRoleResponse>> getGlobalRolesForUser(@PathVariable Long userId) {
        return FlowXResponse.<List<UserRoleResponse>>builder()
                .data(userRoleService.getGlobalRolesForUser(userId))
                .message("Global roles retrieved successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Get local roles for user",
            description = "Retrieves all local roles assigned to a specific user.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Local roles retrieved successfully"
                    )
            }
    )
    @GetMapping("/local/{userId}")
    public FlowXResponse<List<UserRoleResponse>> getLocalRolesForUser(@PathVariable Long userId) {
        return FlowXResponse.<List<UserRoleResponse>>builder()
                .data(userRoleService.getNonGlobalRolesForUser(userId))
                .message("Local roles retrieved successfully")
                .code(200)
                .build();
    }

    @GetMapping("my-roles")
    @Operation(
            summary = "Get my roles",
            description = "Retrieves all roles assigned to the current user.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Roles retrieved successfully"
                    )
            }
    )
    public FlowXResponse<List<UserRoleResponse>> getMyRoles() {
        log.info("Fetching roles for the current user");
        return FlowXResponse.<List<UserRoleResponse>>builder()
                .data(userRoleService.getMyRoles())
                .message("My roles retrieved successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Assign role to user",
            description = "Assigns a specific role to a user.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Role assigned successfully"
                    )
            }
    )
    @PostMapping("/assign")
    public FlowXResponse<Void> assignRoleToUser(@RequestBody UserRoleCreateRequest userRoleCreateRequest) {
        userRoleService.assignRoleToUser(userRoleCreateRequest);
        return FlowXResponse.<Void>builder()
                .message("Role assigned successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Delete role from user",
            description = "Deletes a specific role from a user.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Role deleted successfully"
                    )
            }
    )
    @DeleteMapping("/delete/{id}")
    public FlowXResponse<Void> deleteRoleFromUser(@PathVariable Long id) {
        userRoleService.deleteUserRole(id);
        return FlowXResponse.<Void>builder()
                .message("Role deleted successfully")
                .code(200)
                .build();
    }
}
