package project.ii.flowx.applications.service.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationService {
    final UserRepository userRepository;
    final InvalidTokenRepository invalidTokenRepository;
    final AuthenticationManager authenticationManager;
    final UserDetailsServiceImpl userDetailsService;

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
            User user = userRepository.findByEmail(authenticationRequest.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authenticationRequest.getEmail(),
                            authenticationRequest.getPassword()
                    )
            );

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            String token = generateToken(userPrincipal);
            String refreshToken = generateRefreshToken(userPrincipal);

            AuthenticationResponse response = AuthenticationResponse.builder()
                    .token(token)
                    .authenticated(true)
                    .build();

            return new AuthenticationResult(response, refreshToken);
        } catch (BadCredentialsException e) {
            throw new FlowXException(FlowXError.INVALID_PASSWORD, "Invalid password");
        } catch (AuthenticationException e) {
            throw e;
        }
    }

    public AuthenticationResult authenticateByGoogleOAuth2(String email) {
        if(!userRepository.existsByEmail(email))
            throw new FlowXException(FlowXError.NOT_FOUND, "User not found with email: " + email);

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        String token = generateToken((UserPrincipal) userDetails);
        String refreshToken = generateRefreshToken((UserPrincipal) userDetails);

        AuthenticationResponse response = AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();

        return new AuthenticationResult(response, refreshToken);
    }

    @Transactional
    public void logout(LogoutRequest request) {
        try {
            String token = request.getToken();
            if (!StringUtils.hasText(token)) return;
            // Verify token is valid before invalidating
            DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(jwtSecret))
                    .build()
                    .verify(token);

            // Save token to invalidated tokens repository
            InvalidToken invalidToken = new InvalidToken();
            invalidToken.setToken(token);
            invalidTokenRepository.save(invalidToken);
        } catch (JWTVerificationException e) {
            // Token already expired or invalid
        }
    }

    @Transactional
    public RefreshTokenResponse refreshToken(String refreshTokenString) {
        try {
            log.info("Attempting to refresh token for: {}", refreshTokenString.substring(0, Math.min(10, refreshTokenString.length())) + "...");
            
            // Verify refresh token
            DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(jwtSecret))
                    .build()
                    .verify(refreshTokenString);

            // Check if token is invalidated
            if (invalidTokenRepository.existsByToken(refreshTokenString)) {
                log.warn("Refresh token is already invalidated");
                throw new FlowXException(FlowXError.UNAUTHORIZED, "Refresh token is invalidated");
            }

            // Extract user information from refresh token
            Long userId = decodedJWT.getClaim("userId").asLong();
            String email = decodedJWT.getSubject();
            String scope = decodedJWT.getClaim("scope").asString();
            
            log.info("Refreshing token for user: {} (ID: {})", email, userId);

            // Load user details
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            UserPrincipal userPrincipal = (UserPrincipal) userDetails;

            // Generate new tokens
            String newAccessToken = generateToken(userPrincipal);
            String newRefreshToken = generateRefreshToken(userPrincipal);

            // Invalidate old refresh token
            InvalidToken invalidToken = new InvalidToken();
            invalidToken.setToken(refreshTokenString);
            invalidTokenRepository.save(invalidToken);
            
            log.info("Successfully refreshed token for user: {}", email);

            return RefreshTokenResponse.builder()
                    .token(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .build();

        } catch (JWTVerificationException e) {
            log.error("JWT verification failed during refresh: {}", e.getMessage());
            throw new FlowXException(FlowXError.UNAUTHORIZED, "Invalid refresh token");
        } catch (FlowXException e) {
            // Re-throw FlowXException as-is
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during token refresh: {}", e.getMessage(), e);
            throw new FlowXException(FlowXError.INTERNAL_SERVER_ERROR, "Error refreshing token: " + e.getMessage());
        }
    }

    public String generateToken(UserPrincipal userPrincipal) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        String scope = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        return JWT.create()
                .withSubject(userPrincipal.getUsername())
                .withIssuedAt(now)
                .withExpiresAt(expiryDate)
                .withJWTId(UUID.randomUUID().toString())
                .withClaim("userId", userPrincipal.getId())
                .withClaim("scope", scope)
                .sign(Algorithm.HMAC256(jwtSecret));
    }

    public String generateRefreshToken(UserPrincipal userPrincipal) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpiration);

        String scope = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        return JWT.create()
                .withSubject(userPrincipal.getUsername())
                .withIssuedAt(now)
                .withExpiresAt(expiryDate)
                .withJWTId(UUID.randomUUID().toString())
                .withClaim("userId", userPrincipal.getId())
                .withClaim("scope", scope)
                .sign(Algorithm.HMAC256(jwtSecret));
    }
}
