package project.ii.flowx.module.auth.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import project.ii.flowx.dto.Response;
import project.ii.flowx.module.auth.dto.auth.ChangePasswordRequest;
import project.ii.flowx.module.auth.dto.auth.ForgotPasswordRequest;
import project.ii.flowx.module.auth.dto.auth.ResetPasswordRequest;
import project.ii.flowx.module.auth.service.PasswordService;

@RestController
@RequestMapping("api/password")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Password", description = "Password API")
public class PasswordController {

    PasswordService passwordService;


    @PutMapping("/change-password")
    @Operation(
            summary = "Change user password",
            description = "Changes the password of an existing user.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Password changed successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found"
                    )
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    public Response<Void> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        passwordService.changePassword(changePasswordRequest);
        return Response.<Void>builder()
                .message("Password changed successfully")
                .code(200)
                .build();
    }


    @PostMapping("/forgot-password")
    @Operation(
            summary = "Request password reset",
            description = "Sends a password reset email to the user.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Password reset email sent successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found"
                    )
            }
    )
    public Response<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordService.sendPasswordResetEmail(request);
        return Response.<Void>builder()
                .code(200)
                .message("Reset password email sent successfully")
                .build();
    }

    @PostMapping("/reset-password")
    @Operation(
            summary = "Reset password with token",
            description = "Resets user password using the token received via email.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Password reset successfully"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid token or password"
                    )
            }
    )
    public Response<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordService.resetPassword(request);
        return Response.<Void>builder()
                .code(200)
                .message("Password reset successfully")
                .build();
    }


}
