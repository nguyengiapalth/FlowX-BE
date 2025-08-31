package project.ii.flowx.module.auth.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import project.ii.flowx.dto.Response;
import project.ii.flowx.module.auth.service.AuthenticationService;
import project.ii.flowx.module.auth.service.AuthenticationService.AuthenticationResult;
import project.ii.flowx.module.auth.service.PasswordService;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;
import project.ii.flowx.module.auth.dto.auth.AuthenticationRequest;
import project.ii.flowx.module.auth.dto.auth.AuthenticationResponse;
import project.ii.flowx.module.auth.dto.auth.LogoutRequest;
import project.ii.flowx.module.auth.dto.auth.ChangePasswordRequest;
import project.ii.flowx.module.auth.dto.auth.RefreshTokenResponse;
import project.ii.flowx.module.auth.dto.auth.ForgotPasswordRequest;
import project.ii.flowx.module.auth.dto.auth.ResetPasswordRequest;
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
    GoogleTokenVerifier googleTokenVerifier;
    
    @Value("${app.secure-cookies:false}")
    @NonFinal
    boolean secureCookies;

    @PostMapping("/login")
    @Operation(
            summary = "User login",
            description = "Authenticates a user and returns an access token along with setting a refresh token as an HTTP-only cookie.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Authentication successful"
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Invalid credentials"
                    )
            }
    )
    public Response<AuthenticationResponse> login(
            @Valid @RequestBody AuthenticationRequest request, 
            HttpServletResponse httpResponse,
            HttpServletRequest httpRequest) {
        log.info("Logging in user with email: {}", request.getEmail());
        
        // Extract user agent and IP address for session tracking
        String userAgent = httpRequest.getHeader("User-Agent");
        String ipAddress = getClientIpAddress(httpRequest);
        
        AuthenticationResult result = authenticationService.authenticate(request, userAgent, ipAddress);
        
        // Set refresh token as HTTP-only cookie
        setRefreshTokenCookie(httpResponse, result.refreshToken());
        
        return Response.<AuthenticationResponse>builder()
                .data(result.response())
                .code(200)
                .message("Authentication successful")
                .build();
    }

    @PostMapping("/google-oath2")
    public Response<AuthenticationResponse> oath2Login(
            @RequestBody Map<String, String> body,
            HttpServletResponse httpResponse,
            HttpServletRequest httpRequest) {
        try {
            String idToken = body.get("idToken");

            // Validate input
            if (idToken == null || idToken.trim().isEmpty()) {
                throw new FlowXException(FlowXError.INVALID_CREDENTIALS, "Google ID token is required");
            }

            // Verify Google token and extract payload
            GoogleIdToken.Payload payload = googleTokenVerifier.verify(idToken);
            if (payload == null) {
                throw new FlowXException(FlowXError.INVALID_CREDENTIALS, "Invalid Google token");
            }

            String email = payload.getEmail();
            if (email == null || email.trim().isEmpty()) {
                throw new FlowXException(FlowXError.INVALID_CREDENTIALS, "Email not found in Google token");
            }

            // Extract user agent and IP address for session tracking
            String userAgent = httpRequest.getHeader("User-Agent");
            String ipAddress = getClientIpAddress(httpRequest);

            AuthenticationResult result = authenticationService.authenticateByGoogleOAuth2(email, userAgent, ipAddress);

            // Set refresh token as HTTP-only cookie
            setRefreshTokenCookie(httpResponse, result.refreshToken());

            return Response.<AuthenticationResponse>builder()
                    .data(result.response())
                    .code(200)
                    .message("Google authentication successful")
                    .build();

        } catch (FlowXException e) {
            throw e;
        } catch (Exception e) {
            log.error("Google authentication failed: {}", e.getMessage(), e);
            throw new FlowXException(FlowXError.INVALID_CREDENTIALS, "Google authentication failed");
        }
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "bearerAuth")
    public Response<Void> logout(@Valid @RequestBody LogoutRequest request,
                                 HttpServletResponse httpResponse,
                                 HttpServletRequest httpRequest) {
        // Get refresh token from cookie to revoke the session
        String refreshToken = getRefreshTokenFromCookie(httpRequest);
        
        // Logout with refresh token session revocation
        if (refreshToken != null) {
            authenticationService.logout(request, refreshToken);
        } else {
            authenticationService.logout(request);
        }
        
        // Clear refresh token cookie
        clearRefreshTokenCookie(httpResponse);
        
        return Response.<Void>builder()
                .code(200)
                .message("Logout successful")
                .build();
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
    public Response<AuthenticationResponse> refreshToken(
            HttpServletRequest request, 
            HttpServletResponse response) {
        try {
            log.info("Refresh token request received from: {}", request.getRemoteAddr());
            
            String refreshToken = getRefreshTokenFromCookie(request);
            
            if (refreshToken == null) {
                log.warn("No refresh token found in cookies");
                throw new FlowXException(FlowXError.UNAUTHENTICATED, "Refresh token not found");
            }
            
            // Extract user agent and IP address for new session tracking
            String userAgent = request.getHeader("User-Agent");
            String ipAddress = getClientIpAddress(request);
            
            log.info("Found refresh token in cookie, processing...");
            RefreshTokenResponse tokenResponse = authenticationService.refreshToken(refreshToken, userAgent, ipAddress);
            
            // Set new refresh token as cookie
            setRefreshTokenCookie(response, tokenResponse.getRefreshToken());
            
            AuthenticationResponse authResponse = AuthenticationResponse.builder()
                    .token(tokenResponse.getToken())
                    .authenticated(true)
                    .build();
            
            log.info("Token refresh completed successfully");
            return Response.<AuthenticationResponse>builder()
                    .data(authResponse)
                    .code(200)
                    .message("Token refreshed successfully")
                    .build();
                    
        } catch (FlowXException e) {
            log.error("FlowX error during token refresh: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during token refresh: {}", e.getMessage(), e);
            throw new FlowXException(FlowXError.INTERNAL_SERVER_ERROR, "Token refresh failed: " + e.getMessage());
        }
    }

    @PostMapping("/logout-all-devices")
    @Operation(
            summary = "Logout from all devices",
            description = "Revokes all refresh token sessions for the current user.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Logged out from all devices successfully"
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "User not authenticated"
                    )
            }
    )
    @SecurityRequirement(name = "bearerAuth")
    public Response<Void> logoutFromAllDevices(HttpServletResponse httpResponse) {
        // Get current user ID from security context
        var context = org.springframework.security.core.context.SecurityContextHolder.getContext();
        if (context.getAuthentication() == null || context.getAuthentication().getPrincipal() == null) {
            throw new FlowXException(FlowXError.UNAUTHENTICATED, "User not authenticated");
        }
        
        project.ii.flowx.security.UserPrincipal userPrincipal = 
            (project.ii.flowx.security.UserPrincipal) context.getAuthentication().getPrincipal();
        
        authenticationService.logoutFromAllDevices(userPrincipal.getId());
        
        // Clear current refresh token cookie as well
        clearRefreshTokenCookie(httpResponse);
        
        return Response.<Void>builder()
                .code(200)
                .message("Logged out from all devices successfully")
                .build();
    }

//    @GetMapping("/sessions")
//    @Operation(
//            summary = "Get active sessions",
//            description = "Returns information about all active sessions for the current user.",
//            responses = {
//                    @ApiResponse(
//                            responseCode = "200",
//                            description = "Active sessions retrieved successfully"
//                    ),
//                    @ApiResponse(
//                            responseCode = "401",
//                            description = "User not authenticated"
//                    )
//            }
//    )
//    @SecurityRequirement(name = "bearerAuth")
//    public Response<Map<String, Object>> getActiveSessions() {
//        // Get current user ID from security context
//        var context = org.springframework.security.core.context.SecurityContextHolder.getContext();
//        if (context.getAuthentication() == null || context.getAuthentication().getPrincipal() == null) {
//            throw new FlowXException(FlowXError.UNAUTHENTICATED, "User not authenticated");
//        }
//
//        project.ii.flowx.security.UserPrincipal userPrincipal =
//            (project.ii.flowx.security.UserPrincipal) context.getAuthentication().getPrincipal();
//
//        int activeSessionCount = authenticationService.sessionService.getActiveSessionCount(userPrincipal.getId());
//        java.util.Set<String> sessionIds = authenticationService.sessionService.getUserActiveSessions(userPrincipal.getId());
//
//        Map<String, Object> sessionInfo = Map.of(
//            "activeSessionCount", activeSessionCount,
//            "sessionIds", sessionIds != null ? sessionIds : java.util.Set.of(),
//            "maxSessionsPerUser", 5 // Could be made configurable
//        );
//
//        return Response.<Map<String, Object>>builder()
//                .data(sessionInfo)
//                .code(200)
//                .message("Active sessions retrieved successfully")
//                .build();
//    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(secureCookies); // Configurable based on environment
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
        // Add SameSite attribute for additional security
        response.setHeader("Set-Cookie", 
            String.format("refreshToken=%s; HttpOnly; Path=/; Max-Age=%d; SameSite=Strict%s", 
                refreshToken, 7 * 24 * 60 * 60, secureCookies ? "; Secure" : ""));
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
        // Clear cookie using Set-Cookie header for better compatibility
        response.setHeader("Set-Cookie", 
            String.format("refreshToken=; HttpOnly; Path=/; Max-Age=0; SameSite=Strict%s", 
                secureCookies ? "; Secure" : ""));
    }
    
    /**
     * Extract client IP address from request, considering proxy headers
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            // X-Forwarded-For can contain multiple IPs, take the first one
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        String xForwardedForCloudflare = request.getHeader("CF-Connecting-IP");
        if (xForwardedForCloudflare != null && !xForwardedForCloudflare.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedForCloudflare)) {
            return xForwardedForCloudflare;
        }
        
        // Fallback to remote address
        return request.getRemoteAddr();
    }
}
