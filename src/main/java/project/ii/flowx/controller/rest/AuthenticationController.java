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
import org.springframework.web.bind.annotation.*;
import project.ii.flowx.applications.service.auth.AuthenticationService;
import project.ii.flowx.applications.service.auth.AuthenticationService.AuthenticationResult;
import project.ii.flowx.applications.service.auth.PasswordService;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;
import project.ii.flowx.model.dto.FlowXResponse;
import project.ii.flowx.model.dto.auth.AuthenticationRequest;
import project.ii.flowx.model.dto.auth.AuthenticationResponse;
import project.ii.flowx.model.dto.auth.LogoutRequest;
import project.ii.flowx.model.dto.auth.ChangePasswordRequest;
import project.ii.flowx.model.dto.auth.RefreshTokenResponse;
import project.ii.flowx.model.dto.auth.ForgotPasswordRequest;
import project.ii.flowx.model.dto.auth.ResetPasswordRequest;
import project.ii.flowx.security.GoogleTokenVerifier;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

@RestController
@RequestMapping("api/authentication")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication API")
public class AuthenticationController {
    AuthenticationService authenticationService;
    PasswordService passwordService;
    GoogleTokenVerifier googleTokenVerifier;

    @PostMapping("/login")
    public FlowXResponse<AuthenticationResponse> login(
            @RequestBody AuthenticationRequest request, 
            HttpServletResponse httpResponse) {
        log.info("Logging in user with email: {}", request.getEmail());
        AuthenticationResult result = authenticationService.authenticate(request);
        
        // Set refresh token as HTTP-only cookie
        setRefreshTokenCookie(httpResponse, result.refreshToken());
        
        return FlowXResponse.<AuthenticationResponse>builder()
                .data(result.response())
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
        passwordService.changePassword(changePasswordRequest);
        return FlowXResponse.<Void>builder()
                .message("Password changed successfully")
                .code(200)
                .build();
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "bearerAuth")
    public FlowXResponse<Void> logout(@RequestBody LogoutRequest request, HttpServletResponse httpResponse) {
        authenticationService.logout(request);
        
        // Clear refresh token cookie
        clearRefreshTokenCookie(httpResponse);
        
        return FlowXResponse.<Void>builder()
                .code(200)
                .message("Logout successful")
                .build();
    }

    @PostMapping("/google-oath2")
    public FlowXResponse<AuthenticationResponse> authenticate(
            @RequestBody Map<String, String> body,
            HttpServletResponse httpResponse) {
        try {
            String idToken = body.get("idToken");
            GoogleIdToken.Payload payload = googleTokenVerifier.verify(idToken);
            String email = payload.getEmail();

            AuthenticationResult result = authenticationService.authenticateByGoogleOAuth2(email);
            
            // Set refresh token as HTTP-only cookie
            setRefreshTokenCookie(httpResponse, result.refreshToken());
            
            return FlowXResponse.<AuthenticationResponse>builder()
                    .data(result.response())
                    .code(200)
                    .message("Google authentication successful")
                    .build();

        } catch (Exception e) {
            throw new FlowXException(FlowXError.UNAUTHORIZED, "Google authentication failed: " + e.getMessage());
        }
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh access token",
            description = "Refresh access token using refresh token from HTTP-only cookie.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Token refreshed successfully"
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Refresh token not found or invalid"
                    )
            }
    )
    public FlowXResponse<AuthenticationResponse> refreshToken(
            HttpServletRequest request, 
            HttpServletResponse response) {
        String refreshToken = getRefreshTokenFromCookie(request);
        
        if (refreshToken == null) {
            throw new FlowXException(FlowXError.UNAUTHORIZED, "Refresh token not found");
        }
        
        RefreshTokenResponse tokenResponse = authenticationService.refreshToken(refreshToken);
        
        // Set new refresh token as cookie
        setRefreshTokenCookie(response, tokenResponse.getRefreshToken());
        
        AuthenticationResponse authResponse = AuthenticationResponse.builder()
                .token(tokenResponse.getToken())
                .authenticated(true)
                .build();
        
        return FlowXResponse.<AuthenticationResponse>builder()
                .data(authResponse)
                .code(200)
                .message("Token refreshed successfully")
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
    public FlowXResponse<Void> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        passwordService.sendPasswordResetEmail(request);
        return FlowXResponse.<Void>builder()
                .code(200)
                .message("Nếu email tồn tại trong hệ thống, một email đặt lại mật khẩu đã được gửi")
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
    public FlowXResponse<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        passwordService.resetPassword(request);
        return FlowXResponse.<Void>builder()
                .code(200)
                .message("Mật khẩu đã được đặt lại thành công")
                .build();
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true); // Use HTTPS in production
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
        response.addCookie(refreshTokenCookie);
    }

    private String getRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie refreshTokenCookie = new Cookie("refreshToken", "");
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0); // Expire immediately
        response.addCookie(refreshTokenCookie);
    }

}
