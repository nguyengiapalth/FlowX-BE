//package project.ii.flowx.controller;
//
//
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.security.SecurityRequirement;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.AccessLevel;
//import lombok.RequiredArgsConstructor;
//import lombok.experimental.FieldDefaults;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.web.bind.annotation.*;
//import project.ii.flowx.applications.service.auth.AuthorizationService;
//import project.ii.flowx.model.dto.FlowXResponse;
//import project.ii.flowx.model.dto.userrole.UserRoleResponse;
//import project.ii.flowx.shared.enums.RoleScope;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/authorize")
//@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
//@RequiredArgsConstructor
//@Slf4j
//@CrossOrigin(origins = "*")
//@Tag(name = "User Role Check", description = "User Role API")
//@SecurityRequirement(name = "bearerAuth")
//public class AuthorizeCTest {
//    AuthorizationService authorizationService;
//    @Operation(
//            summary = "Get current user ID",
//            description = "Retrieves the ID of the currently authenticated user.",
//            responses = {
//                    @ApiResponse(responseCode = "200", description = "User ID retrieved successfully"),
//                    @ApiResponse(responseCode = "401", description = "Unauthorized")
//            }
//    )
//    @GetMapping("/current-user-id")
//    public FlowXResponse<Long> getCurrentUserId() {
//        log.info("Fetching current user ID");
//        return FlowXResponse.<Long>builder()
//                .data(authorizationService.getUserId())
//                .message("Current user ID retrieved successfully")
//                .code(200)
//                .build();
//    }
//
//    @Operation(
//            summary = "Get user roles",
//            description = "Retrieves all roles for a specific user.",
//            responses = {
//                    @ApiResponse(responseCode = "200", description = "User roles retrieved successfully")
//            }
//    )
//    @GetMapping("/user-roles/{userId}")
//    public FlowXResponse<List<UserRoleResponse>> getUserRoles(@PathVariable Long userId) {
//        log.info("Fetching roles for user ID: {}", userId);
//        return FlowXResponse.<List<UserRoleResponse>>builder()
//                .data(authorizationService.getUserAllRoles(userId))
//                .message("User roles retrieved successfully")
//                .code(200)
//                .build();
//    }
//
//    @Operation(
//            summary = "Check specific role",
//            description = "Checks if current user has the specified role in the given scope.",
//            responses = {
//                    @ApiResponse(responseCode = "200", description = "Role check completed")
//            }
//    )
//    @GetMapping("/check-role")
//    public FlowXResponse<Boolean> hasRole(
//            @RequestParam String roleName,
//            @RequestParam RoleScope roleScope,
//            @RequestParam Long scopeId) {
//        log.info("Checking if user has role: {} in scope: {} with ID: {}", roleName, roleScope, scopeId);
//        return FlowXResponse.<Boolean>builder()
//                .data(authorizationService.hasRole(roleName, roleScope, scopeId))
//                .message("Role check completed")
//                .code(200)
//                .build();
//    }
//
//    @Operation(
//            summary = "Check project role",
//            description = "Checks if current user has the specified role in the given project.",
//            responses = {
//                    @ApiResponse(responseCode = "200", description = "Project role check completed")
//            }
//    )
//    @GetMapping("/check-project-role")
//    public FlowXResponse<Boolean> hasProjectRole(
//            @RequestParam String roleName,
//            @RequestParam Long projectId) {
//        log.info("Checking if user has project role: {} for project ID: {}", roleName, projectId);
//        return FlowXResponse.<Boolean>builder()
//                .data(authorizationService.hasProjectRole(roleName, projectId))
//                .message("Project role check completed")
//                .code(200)
//                .build();
//    }
//
//    @Operation(
//            summary = "Check department role",
//            description = "Checks if current user has the specified role in the given department.",
//            responses = {
//                    @ApiResponse(responseCode = "200", description = "Department role check completed")
//            }
//    )
//    @GetMapping("/check-department-role")
//    public FlowXResponse<Boolean> hasDepartmentRole(
//            @RequestParam String roleName,
//            @RequestParam Long departmentId) {
//        log.info("Checking if user has department role: {} for department ID: {}", roleName, departmentId);
//        return FlowXResponse.<Boolean>builder()
//                .data(authorizationService.hasDepartmentRole(roleName, departmentId))
//                .message("Department role check completed")
//                .code(200)
//                .build();
//    }
//}
