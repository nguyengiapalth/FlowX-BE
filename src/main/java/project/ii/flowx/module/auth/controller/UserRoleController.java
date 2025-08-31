package project.ii.flowx.module.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import project.ii.flowx.dto.Response;
import project.ii.flowx.module.auth.service.UserRoleService;
import project.ii.flowx.module.auth.dto.userrole.UserRoleResponse;
import project.ii.flowx.security.UserPrincipal;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/user-role")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "User Role", description = "User Role API")
@SecurityRequirement(name = "bearerAuth")
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
    public Response<List<UserRoleResponse>> getRolesForUser(@PathVariable UUID userId) {
        return Response.<List<UserRoleResponse>>builder()
                .data(userRoleService.getRolesForUser(userId))
                .message("User roles retrieved successfully")
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
    public Response<List<UserRoleResponse>> getMyRoles() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID userId = userPrincipal.getId();

        log.info("Fetching roles for the current user");
        return Response.<List<UserRoleResponse>>builder()
                .data(userRoleService.getRolesForUser(userId))
                .message("My roles retrieved successfully")
                .code(200)
                .build();
    }
}
