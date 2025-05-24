package project.ii.flowx.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.ii.flowx.applications.service.auth.AuthenticationService;
import project.ii.flowx.model.dto.FlowXResponse;
import project.ii.flowx.model.dto.auth.AuthenticationRequest;
import project.ii.flowx.model.dto.auth.AuthenticationResponse;
import project.ii.flowx.model.dto.auth.LogoutRequest;
import project.ii.flowx.model.dto.auth.ChangePasswordRequest;

@RestController
@RequestMapping("api/authentication")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Authentication", description = "Authentication API")
public class AuthenticationController {
    AuthenticationService authenticationService;

    @PostMapping("/login")
    public FlowXResponse<AuthenticationResponse> login(@RequestBody AuthenticationRequest request) {
        log.info("Logging in user with email: {}", request.getEmail());
        AuthenticationResponse response = authenticationService.authenticate(request);
        return FlowXResponse.<AuthenticationResponse>builder()
                .data(response)
                .code(200)
                .message("Authentication successful")
                .build();
    }

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
    public FlowXResponse<Object> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest) {
        authenticationService.changePassword(changePasswordRequest);
        return FlowXResponse.builder()
                .message("Password changed successfully")
                .code(200)
                .build();
    }

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public ResponseEntity<FlowXResponse<Void>> logout(@RequestBody LogoutRequest request) {
        authenticationService.logout(request);
        return ResponseEntity.ok(FlowXResponse.<Void>builder()
                .code(1000)
                .message("Logout successful")
                .build());
    }
}
