package project.ii.flowx.applications.service.auth;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import project.ii.flowx.model.entity.InvalidToken;
import project.ii.flowx.model.entity.User;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;
import project.ii.flowx.model.dto.auth.AuthenticationRequest;
import project.ii.flowx.model.dto.auth.AuthenticationResponse;
import project.ii.flowx.model.dto.auth.LogoutRequest;
import project.ii.flowx.model.dto.auth.RefreshTokenResponse;
import project.ii.flowx.model.repository.InvalidTokenRepository;
import project.ii.flowx.model.repository.UserRepository;
import project.ii.flowx.security.UserDetailsServiceImpl;
import project.ii.flowx.security.UserPrincipal;

import java.util.Date;
import java.util.Map;
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
    final project.ii.flowx.security.FlowXJwtDecoder jwtDecoder;

    @Value("${spring.jwt.secret}")
    String jwtSecret;

    @Value("${spring.jwt.expiration}")
    long jwtExpiration;

    @Value("${spring.jwt.refresh-expiration}")
    long refreshExpiration;

    public record AuthenticationResult(AuthenticationResponse response, String refreshToken) { }

    @Transactional(readOnly = true)
    public AuthenticationResult authenticate(AuthenticationRequest authenticationRequest) {
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
        if(!userRepository.existsByEmail(email))
            throw new FlowXException(FlowXError.NOT_FOUND, "User not found with email: " + email);

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        String token = generateToken((UserPrincipal) userDetails, jwtExpiration);
        String refreshToken = generateToken((UserPrincipal) userDetails, refreshExpiration);

        AuthenticationResponse response = AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();

        return new AuthenticationResult(response, refreshToken);
    }

    @Transactional
    @PreAuthorize("isAuthenticated()")
    public void logout(LogoutRequest request) {
        try {
            String token = request.getToken();
            if (!StringUtils.hasText(token)) return;
            // Save token to invalidated tokens repository
            InvalidToken invalidToken = new InvalidToken();
            invalidToken.setToken(token);
            invalidTokenRepository.save(invalidToken);
        } catch (JwtException e) {
            // Token already expired or invalid
            log.debug("Token validation failed during logout: {}", e.getMessage());
        }
    }

    @Transactional
    public RefreshTokenResponse refreshToken(String refreshTokenString) {
        try {
            log.info("Attempting to refresh token for: {}", refreshTokenString.substring(0, Math.min(10, refreshTokenString.length())) + "...");
            
            // Verify refresh token using Nimbus decoder
            Jwt jwt = jwtDecoder.decode(refreshTokenString);

            // Check if token is invalidated
            if (invalidTokenRepository.existsByToken(refreshTokenString)) {
                log.warn("Refresh token is already invalidated");
                throw new FlowXException(FlowXError.INVALID_TOKEN, "Refresh token is invalidated");
            }
            String email = jwt.getSubject();

            // Load user details
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            UserPrincipal userPrincipal = (UserPrincipal) userDetails;

            // Generate new tokens
            String newAccessToken = generateToken(userPrincipal, jwtExpiration);
            String newRefreshToken = generateToken(userPrincipal, refreshExpiration);

            // Invalidate old refresh token
            InvalidToken invalidToken = new InvalidToken();
            invalidToken.setToken(refreshTokenString);
            invalidTokenRepository.save(invalidToken);
            
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
            throw new FlowXException(FlowXError.INTERNAL_SERVER_ERROR, "Error refreshing token: " + e.getMessage());
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
