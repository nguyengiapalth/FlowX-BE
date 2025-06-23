package project.ii.flowx.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import project.ii.flowx.model.repository.InvalidTokenRepository;
import project.ii.flowx.model.dto.FlowXResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Custom JWT authentication filter that validates JWT tokens and sets the authentication in the security context.
 * It handles token expiration and invalid tokens by returning appropriate JSON responses.
 * This filter is applied to incoming HTTP requests to secure endpoints.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final InvalidTokenRepository invalidTokenRepository;
    private final FlowXJwtDecoder jwtDecoder;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response, @NotNull FilterChain filterChain)
            throws ServletException, IOException {
        log.info(request.getRequestURI());

        try {
            String jwt = getJwtFromRequest(request);
            if (StringUtils.hasText(jwt)) {
                // Use FlowXJwtDecoder for token validation and parsing
                Jwt decodedJWT = jwtDecoder.decode(jwt);
                
                Long id = decodedJWT.getClaim("userId");
                String email = decodedJWT.getSubject();
                String scope = decodedJWT.getClaim("scope");
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
        } catch (JwtException ex) {
            log.error("JWT verification failed: {}", ex.getMessage());
            
            // Check if it's an expired token based on the message
            if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("expired")) {
                handleTokenExpired(response);
            } else {
                handleInvalidToken(response);
            }
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
