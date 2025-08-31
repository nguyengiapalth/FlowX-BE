package project.ii.flowx.module.auth.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import project.ii.flowx.module.auth.entity.InvalidToken;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;
import project.ii.flowx.module.auth.dto.auth.AuthenticationRequest;
import project.ii.flowx.module.auth.dto.auth.AuthenticationResponse;
import project.ii.flowx.module.auth.dto.auth.LogoutRequest;
import project.ii.flowx.module.auth.dto.auth.RefreshTokenResponse;
import project.ii.flowx.module.auth.repository.InvalidTokenRepository;
import project.ii.flowx.module.user.repository.UserRepository;
import project.ii.flowx.security.FlowXJwtDecoder;
import project.ii.flowx.security.JwtDecoderImpl;
import project.ii.flowx.security.UserDetailsServiceImpl;
import project.ii.flowx.security.UserPrincipal;

import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.Instant;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationService {
    final UserRepository userRepository;
    final InvalidTokenRepository invalidTokenRepository;
    final AuthenticationManager authenticationManager;
    final UserDetailsServiceImpl userDetailsService;
    final JwtDecoderImpl jwtDecoder;
    final RefreshTokenSessionService sessionService;

    @Value("${spring.jwt.secret}")
    String jwtSecret;

    @Value("${spring.jwt.expiration}")
    long jwtExpiration;

    @Value("${spring.jwt.refresh-expiration}")
    long refreshExpiration;

    public record AuthenticationResult(AuthenticationResponse response, String refreshToken) { }

    @Transactional(readOnly = true)
    public AuthenticationResult authenticate(AuthenticationRequest authenticationRequest) {
        return authenticate(authenticationRequest, null, null);
    }
    
    @Transactional(readOnly = true)
    public AuthenticationResult authenticate(AuthenticationRequest authenticationRequest, String userAgent, String ipAddress) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authenticationRequest.getEmail(),
                            authenticationRequest.getPassword()
                    )
            );

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            String token = generateToken(userPrincipal, jwtExpiration);
            String refreshToken = generateToken(userPrincipal, refreshExpiration);

            // Store refresh token session in Redis
            try {
                Jwt refreshJwt = jwtDecoder.decode(refreshToken);
                sessionService.storeRefreshTokenSession(
                    refreshJwt.getId(),
                    userPrincipal.getId(),
                    refreshJwt.getExpiresAt(),
                    userAgent,
                    ipAddress
                );
                log.info("Stored refresh token session for user: {}", userPrincipal.getId());
            } catch (Exception e)
            {
                log.error("Failed to store refresh token session: {}", e.getMessage(), e);
            }

            AuthenticationResponse response = AuthenticationResponse.builder()
                    .token(token)
                    .authenticated(true)
                    .build();

            return new AuthenticationResult(response, refreshToken);
        } catch (BadCredentialsException e) {
            throw new FlowXException(FlowXError.INVALID_PASSWORD, "Invalid password");
        }
    }

    public AuthenticationResult authenticateByGoogleOAuth2(String email) {
        return authenticateByGoogleOAuth2(email, null, null);
    }
    
    public AuthenticationResult authenticateByGoogleOAuth2(String email, String userAgent, String ipAddress) {
        // Validate email format
        if (email == null || email.trim().isEmpty() || !email.contains("@")) {
            throw new FlowXException(FlowXError.INVALID_CREDENTIALS, "Invalid email format");
        }
        
        // Check if user exists
        if (!userRepository.existsByEmail(email)) {
            throw new FlowXException(FlowXError.NOT_FOUND, "User not found with email: " + email);
        }

        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            
            // Verify user is active
            if (!userDetails.isEnabled() || !userDetails.isAccountNonLocked()) {
                throw new FlowXException(FlowXError.ACCESS_DENIED, "User account is disabled or locked");
            }

            UserPrincipal userPrincipal = (UserPrincipal) userDetails;
            String token = generateToken(userPrincipal, jwtExpiration);
            String refreshToken = generateToken(userPrincipal, refreshExpiration);

            // Store refresh token session in Redis
            try {
                Jwt refreshJwt = jwtDecoder.decode(refreshToken);
                sessionService.storeRefreshTokenSession(
                    refreshJwt.getId(),
                    userPrincipal.getId(),
                    refreshJwt.getExpiresAt(),
                    userAgent,
                    ipAddress
                );
                log.info("Stored refresh token session for Google OAuth user: {}", userPrincipal.getId());
            } catch (Exception e) {
                log.error("Failed to store refresh token session for Google OAuth: {}", e.getMessage(), e);
            }

            AuthenticationResponse response = AuthenticationResponse.builder()
                    .token(token)
                    .authenticated(true)
                    .build();

            return new AuthenticationResult(response, refreshToken);
        } catch (UsernameNotFoundException e) {
            throw new FlowXException(FlowXError.NOT_FOUND, "User not found");
        }
    }

    @Transactional
    @PreAuthorize("isAuthenticated()")
    public void logout(LogoutRequest request) {
        try {
            String token = request.getToken();
            if (!StringUtils.hasText(token)) return;
            
            // Keep existing access token invalidation for backwards compatibility
            InvalidToken invalidToken = new InvalidToken();
            invalidToken.setToken(token);
            invalidTokenRepository.save(invalidToken);
            
        } catch (JwtException e) {
            // Token already expired or invalid
            log.debug("Token validation failed during logout: {}", e.getMessage());
        }
    }
    
    /**
     * Logout and revoke specific refresh token session
     */
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public void logout(LogoutRequest request, String refreshToken) {
        // Standard logout
        logout(request);
        
        // Revoke refresh token session if provided
        if (StringUtils.hasText(refreshToken)) {
            try {
                Jwt refreshJwt = jwtDecoder.decode(refreshToken);
                String tokenId = refreshJwt.getId();
                sessionService.revokeSession(tokenId);
                log.info("Revoked refresh token session: {}", tokenId);
            } catch (Exception e) {
                log.debug("Failed to revoke refresh token session during logout: {}", e.getMessage());
            }
        }
    }
    
    /**
     * Logout from all devices - revoke all user sessions
     */
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public void logoutFromAllDevices(UUID userId) {
        try {
            sessionService.revokeAllUserSessions(userId);
            log.info("Revoked all sessions for user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to revoke all sessions for user: {} - {}", userId, e.getMessage(), e);
            throw new FlowXException(FlowXError.INTERNAL_SERVER_ERROR, "Failed to logout from all devices");
        }
    }

    @Transactional
    public RefreshTokenResponse refreshToken(String refreshTokenString) {
        return refreshToken(refreshTokenString, null, null);
    }
    
    @Transactional
    public RefreshTokenResponse refreshToken(String refreshTokenString, String userAgent, String ipAddress) {
        try {
            log.info("Attempting to refresh token for: {}", refreshTokenString.substring(0, Math.min(10, refreshTokenString.length())) + "...");
            
            // Verify refresh token using Nimbus decoder
            Jwt jwt = jwtDecoder.decode(refreshTokenString);
            String tokenId = jwt.getId(); // JTI claim
            String email = jwt.getSubject();

            // Use Redis-based validation with distributed locking to prevent race conditions
            if (!sessionService.validateAndInvalidateToken(tokenId)) {
                log.warn("Refresh token is invalid or already used: {}", tokenId);
                throw new FlowXException(FlowXError.INVALID_TOKEN, "Refresh token is invalid or already used");
            }

            // Load user details
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            UserPrincipal userPrincipal = (UserPrincipal) userDetails;

            // Generate new tokens after invalidating the old one
            String newAccessToken = generateToken(userPrincipal, jwtExpiration);
            String newRefreshToken = generateToken(userPrincipal, refreshExpiration);
            
            // Store new refresh token session in Redis
            try {
                Jwt newRefreshJwt = jwtDecoder.decode(newRefreshToken);
                sessionService.storeRefreshTokenSession(
                    newRefreshJwt.getId(),
                    userPrincipal.getId(),
                    newRefreshJwt.getExpiresAt(),
                    userAgent,
                    ipAddress
                );
                log.info("Stored new refresh token session for user: {}", userPrincipal.getId());
            } catch (Exception e) {
                log.error("Failed to store new refresh token session: {}", e.getMessage(), e);
                // Continue with token refresh even if session storage fails
            }
            
            log.info("Successfully refreshed token for user: {}", email);

            return RefreshTokenResponse.builder()
                    .token(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .build();

        } catch (JwtException e) {
            log.error("JWT verification failed during refresh: {}", e.getMessage());
            throw new FlowXException(FlowXError.INVALID_TOKEN, "Invalid refresh token");
        } catch (FlowXException e) {
            // Re-throw FlowXException as-is
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during token refresh: {}", e.getMessage(), e);
            throw new FlowXException(FlowXError.INTERNAL_SERVER_ERROR, "Token refresh failed");
        }
    }

    public String generateToken(UserPrincipal userPrincipal, long expiration) {
        try {
            Instant now = Instant.now();
            Instant expiryTime = now.plusMillis(expiration);

            String scope = userPrincipal.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(" "));

            // Create JWT claims
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(userPrincipal.getUsername())
                    .issuer("FlowX") // Match the issuer in FlowXJwtDecoder
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(expiryTime))
                    .jwtID(UUID.randomUUID().toString())
                    .claim("userId", userPrincipal.getId())
                    .claim("scope", scope)
                    .build();

            // Create JWT header
            JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);

            // Create signed JWT
            SignedJWT signedJWT = new SignedJWT(header, claimsSet);

            // Sign the JWT with HMAC256
            MACSigner signer = new MACSigner(jwtSecret.getBytes());
            signedJWT.sign(signer);

            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Failed to generate JWT token", e);
        }
    }
}
