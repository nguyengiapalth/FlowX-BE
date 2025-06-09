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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import project.ii.flowx.model.entity.InvalidToken;
import project.ii.flowx.model.entity.User;
import project.ii.flowx.model.dto.auth.ChangePasswordRequest;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;
import project.ii.flowx.model.dto.auth.AuthenticationRequest;
import project.ii.flowx.model.dto.auth.AuthenticationResponse;
import project.ii.flowx.model.dto.auth.LogoutRequest;
import project.ii.flowx.model.dto.auth.RefreshTokenRequest;
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
    final PasswordEncoder passwordEncoder;
    final AuthenticationManager authenticationManager;
    final UserDetailsServiceImpl userDetailsService;

    @Value("${spring.jwt.secret}")
    String jwtSecret;

    @Value("${spring.jwt.expiration}")
    long jwtExpiration;

    @Value("${spring.jwt.refresh-expiration}")
    long refreshExpiration;

    public static class AuthenticationResult {
        public final AuthenticationResponse response;
        public final String refreshToken;
        
        public AuthenticationResult(AuthenticationResponse response, String refreshToken) {
            this.response = response;
            this.refreshToken = refreshToken;
        }
    }

    @Transactional(readOnly = true)
    public AuthenticationResult authenticate(AuthenticationRequest authenticationRequest) {
        try {
            log.info("Authentication request for user {}", authenticationRequest.getEmail());

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
            log.warn("Sai mật khẩu cho user {}", authenticationRequest.getEmail());
            throw new FlowXException(FlowXError.INVALID_PASSWORD, "Invalid password");
        } catch (AuthenticationException e) {
            log.error("Lỗi xác thực khác: {}", e.getMessage());
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
        log.info("Authentication request for user {}", email);
        log.info("Authorities: {}", userDetails.getAuthorities());

        AuthenticationResponse response = AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();

        return new AuthenticationResult(response, refreshToken);
    }


    @Transactional(readOnly = true)
    public RefreshTokenResponse refreshToken(String refreshTokenString) {
        try {
            // Verify refresh token
            DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(jwtSecret))
                    .build()
                    .verify(refreshTokenString);

            // Check if token is invalidated
            if (invalidTokenRepository.existsByToken(refreshTokenString)) {
                throw new FlowXException(FlowXError.UNAUTHORIZED, "Refresh token is invalidated");
            }

            // Extract user information from refresh token
            Long userId = decodedJWT.getClaim("userId").asLong();
            String email = decodedJWT.getSubject();
            String scope = decodedJWT.getClaim("scope").asString();

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

            return RefreshTokenResponse.builder()
                    .token(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .build();

        } catch (JWTVerificationException e) {
            throw new FlowXException(FlowXError.UNAUTHORIZED, "Invalid refresh token");
        } catch (Exception e) {
            throw new FlowXException(FlowXError.INTERNAL_SERVER_ERROR, "Error refreshing token");
        }
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
            log.info("Token invalidated successfully");
        } catch (JWTVerificationException e) {
            log.info("Token already expired or invalid: {}", e.getMessage());
        }
    }

    @Transactional
    public void changePassword(ChangePasswordRequest changePasswordRequest) {
        var context = SecurityContextHolder.getContext();
        if (context == null || context.getAuthentication() == null) {
            throw new FlowXException(FlowXError.UNAUTHORIZED, "Unauthorized");
        }
        Authentication authentication = context.getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "User not found"));

        log.info("old password: {}", changePasswordRequest.getOldPassword());
        log.info("is matches: {}", passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPassword()));

        if(!passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPassword()))
            throw new FlowXException(FlowXError.INVALID_PASSWORD, "Invalid password");

        user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));

        userRepository.save(user);
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
