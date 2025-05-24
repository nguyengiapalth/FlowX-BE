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
import project.ii.flowx.model.dto.FlowXResponse;
import project.ii.flowx.model.dto.user.UserCreateRequest;
import project.ii.flowx.model.dto.user.UserResponse;
import project.ii.flowx.model.dto.user.UserUpdateRequest;
import project.ii.flowx.applications.service.manage.UserService;
import project.ii.flowx.shared.enums.UserStatus;

import java.util.List;

@RestController
@RequestMapping("api/user")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")  // Cho phép tất cả các origin
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
    public FlowXResponse<UserResponse> createUser(@RequestBody UserCreateRequest userCreateRequest) {
        log.info("Creating user with details: {}", userCreateRequest);
        return FlowXResponse.<UserResponse>builder()
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
    public FlowXResponse<UserResponse> updateUser(@PathVariable Long id, UserUpdateRequest userUpdateRequest) {
        return FlowXResponse.<UserResponse>builder()
                .data(userService.updateUser(id, userUpdateRequest))
                .message("User updated successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Update user department",
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
    @PutMapping("/change-department/{id}")
    public FlowXResponse<UserResponse> updateUserDepartment(@PathVariable Long id, @RequestParam Long departmentId) {
        return FlowXResponse.<UserResponse>builder()
                .data(userService.updateUserDepartment(id, departmentId))
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
    public FlowXResponse<UserResponse> updateUserStatus(@PathVariable Long id, @RequestParam UserStatus status) {
        return FlowXResponse.<UserResponse>builder()
                .data(userService.updateUserStatus(id, status))
                .message("User updated successfully")
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
    public FlowXResponse<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return FlowXResponse.<Void>builder()
                .message("User deleted successfully")
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
    @GetMapping("/getall")
    public FlowXResponse<List<UserResponse>> getAllUsers() {
        return FlowXResponse.<List<UserResponse>>builder()
                .data(userService.getAllUsers())
                .message("List of users retrieved successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Get users by department",
            description = "Retrieves a list of users by their department ID.",
            parameters = {
                    @Parameter(name = "departmentId", description = "ID of the department")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of users retrieved successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Department not found"
                    )
            }
    )
    @GetMapping("/get-by-department/{departmentId}")
    public FlowXResponse<List<UserResponse>> getUsersByDepartment(@PathVariable Long departmentId) {
        return FlowXResponse.<List<UserResponse>>builder()
                .data(userService.getUsersByDepartment(departmentId))
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
    public FlowXResponse<UserResponse> getUserById(@PathVariable Long id) {
        return FlowXResponse.<UserResponse>builder()
                .data(userService.getUserById(id))
                .message("User retrieved successfully")
                .code(200)
                .build();
    }
}
