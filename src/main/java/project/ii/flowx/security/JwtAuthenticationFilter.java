package project.ii.flowx.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;
import project.ii.flowx.model.repository.InvalidTokenRepository;
import project.ii.flowx.model.dto.FlowXResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final InvalidTokenRepository invalidTokenRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${spring.jwt.secret}")
    private String jwtSecret;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response, @NotNull FilterChain filterChain)
            throws ServletException, IOException {
        log.info(request.getRequestURI());

        try {
            String jwt = getJwtFromRequest(request);
            if (StringUtils.hasText(jwt)) {
                if (validateToken(jwt)) {
                    DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(jwtSecret))
                            .build()
                            .verify(jwt);
                    Long id = decodedJWT.getClaim("userId").asLong();
                    String email = decodedJWT.getSubject();
                    String scope = decodedJWT.getClaim("scope").asString();
                    UserPrincipal userDetails = new UserPrincipal(id, email, null, Collections.emptyList());

                    List<SimpleGrantedAuthority> authorities = Collections.emptyList();

                    if (StringUtils.hasText(scope)) {
                        authorities = Arrays.stream(scope.split(" "))
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList());
                    }

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (TokenExpiredException ex) {
            log.error("Token expired: {}", ex.getMessage());
            handleTokenExpired(response);
            return; // Không tiếp tục filter chain
        } catch (JWTVerificationException ex) {
            log.error("JWT verification failed: {}", ex.getMessage());
            handleInvalidToken(response);
            return; // Không tiếp tục filter chain
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private boolean validateToken(String token) {
        try {
            // Check if token is in the invalidated tokens repository
            if (invalidTokenRepository.existsByToken(token)) return false;

            // Verify token signature
            JWT.require(Algorithm.HMAC256(jwtSecret)).build().verify(token);
            return true;
        } catch (TokenExpiredException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
            throw ex;
//            return false;
        }
    }

    private void handleTokenExpired(HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        FlowXResponse<?> errorResponse = FlowXResponse.builder()
                .code(401)
                .message("Token expired")
                .build();
        
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }

    private void handleInvalidToken(HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        FlowXResponse<?> errorResponse = FlowXResponse.builder()
                .code(401)
                .message("Invalid token")
                .build();
        
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
