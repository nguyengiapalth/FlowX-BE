package project.ii.flowx.module.user.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import project.ii.flowx.module.user.service.UserService;
import project.ii.flowx.module.user.dto.UserCreateRequest;
import project.ii.flowx.module.user.dto.UserResponse;
import project.ii.flowx.module.user.dto.UserUpdateRequest;
import project.ii.flowx.security.UserPrincipal;
import project.ii.flowx.applications.enums.UserStatus;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/user")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "User", description = "User API")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    UserService userService;

    @Operation(
            summary = "Create a new user",
            description = "Creates a new user in the system. " +
                    "The user details are provided in the request body.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User created successfully"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid user details provided"
                    )
            }
    )
    @PostMapping("/create")
    public Response<UserResponse> create(@RequestBody UserCreateRequest userCreateRequest) {
        log.info("Creating user with details: {}", userCreateRequest);
        return Response.<UserResponse>builder()
                .data(userService.createUser(userCreateRequest))
                .message("User created successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Update user",
            description = "Updates the details of an existing user.",
            parameters = {
                    @Parameter(name = "id", description = "ID of the user to be updated")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User updated successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found"
                    )
            }
    )
    @PutMapping("/update/{id}")
    public Response<UserResponse> update(@PathVariable UUID id, UserUpdateRequest userUpdateRequest) {
        return Response.<UserResponse>builder()
                .data(userService.updateUser(id, userUpdateRequest))
                .message("User updated successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Update user status",
            description = "Updates the status of a specific user.",
            parameters = {
                    @Parameter(name = "id", description = "ID of the user to be updated")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User updated successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found"
                    )
            }
    )
    @PutMapping("/update-status/{id}")
    public Response<UserResponse> updateStatus(@PathVariable UUID id, @RequestParam UserStatus status) {
        return Response.<UserResponse>builder()
                .data(userService.updateUserStatus(id, status))
                .message("User updated successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Update user position",
            description = "Updates the position of a specific user.",
            parameters = {
                    @Parameter(name = "id", description = "ID of the user to be updated"),
                    @Parameter(name = "position", description = "New position for the user")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User position updated successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found"
                    )
            }
    )
    @PutMapping("/update-position/{id}")
    public Response<UserResponse> updatePosition(@PathVariable UUID id, @RequestParam String position) {
        return Response.<UserResponse>builder()
                .data(userService.updateUserPosition(id, position))
                .message("User position updated successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "delete user",
            description = "Deletes a user by their unique ID.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User deleted successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found"
                    )
            }
    )
    @DeleteMapping("/delete/{id}")
    public Response<Void> delete(@PathVariable UUID id) {
        userService.deleteUser(id);
        return Response.<Void>builder()
                .message("User deleted successfully")
                .code(200)
                .build();

    }

    @Operation(
            summary = "Get my profile",
            description = "Retrieves the profile of the currently authenticated user.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User profile retrieved successfully"
                    )
            }
    )
    @GetMapping("/my-profile")
    public Response<UserResponse> getMyProfile() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID userId = userPrincipal.getId();


        return Response.<UserResponse>builder()
                .data(userService.getUserById(userId))
                .message("User profile retrieved successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Update my profile",
            description = "Updates the profile of the currently authenticated user.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User profile updated successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found"
                    )
            }
    )
    @PutMapping("/my-profile")
    public Response<UserResponse> updateMyProfile(@RequestBody UserUpdateRequest userUpdateRequest) {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID userId = userPrincipal.getId();

        return Response.<UserResponse>builder()
                .data(userService.updateUser(userId, userUpdateRequest))
                .message("User profile updated successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Get all users",
            description = "Retrieves a list of all users in the system.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of users retrieved successfully"
                    )
            }
    )
    @GetMapping("/get-all")
    public Response<List<UserResponse>> getAllUsers() {
        return Response.<List<UserResponse>>builder()
                .data(userService.getAllUsers())
                .message("List of users retrieved successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Get user by ID",
            description = "Retrieves a user by their unique ID.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User retrieved successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found"
                    )
            }
    )
    @GetMapping("/get/{id}")
    public Response<UserResponse> getUserById(@PathVariable UUID id) {
        return Response.<UserResponse>builder()
                .data(userService.getUserById(id))
                .message("User retrieved successfully")
                .code(200)
                .build();
    }
}
