package project.ii.flowx.controller.rest;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
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
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;
import project.ii.flowx.model.dto.FlowXResponse;
import project.ii.flowx.model.dto.auth.AuthenticationRequest;
import project.ii.flowx.model.dto.auth.AuthenticationResponse;
import project.ii.flowx.model.dto.auth.LogoutRequest;
import project.ii.flowx.model.dto.auth.ChangePasswordRequest;
import project.ii.flowx.security.GoogleTokenVerifier;

import java.util.Map;

@RestController
@RequestMapping("api/authentication")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Authentication", description = "Authentication API")
public class AuthenticationController {
    AuthenticationService authenticationService;
    GoogleTokenVerifier googleTokenVerifier;

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
    public FlowXResponse<Void> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest) {
        authenticationService.changePassword(changePasswordRequest);
        return FlowXResponse.<Void>builder()
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

    @PostMapping("/google-oath2")
    public FlowXResponse<AuthenticationResponse> authenticate(@RequestBody Map<String, String> body) {
        try {
            String idToken = body.get("idToken");
            GoogleIdToken.Payload payload = googleTokenVerifier.verify(idToken);
            String email = payload.getEmail();

            return FlowXResponse.<AuthenticationResponse>builder()
                    .data(authenticationService.authenticateByGoogleOAuth2(email))
                    .code(200)
                    .message("Google authentication successful")
                    .build();

        } catch (Exception e) {
            throw new FlowXException(FlowXError.UNAUTHORIZED, "Google authentication failed: " + e.getMessage());
        }
    }

}
