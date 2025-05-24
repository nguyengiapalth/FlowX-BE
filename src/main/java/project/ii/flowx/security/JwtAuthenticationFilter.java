package project.ii.flowx.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
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
import project.ii.flowx.model.repository.InvalidTokenRepository;

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

    @Value("${spring.jwt.secret}")
    private String jwtSecret;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response, @NotNull FilterChain filterChain)
            throws ServletException, IOException {
        log.info(request.getRequestURI());

        try {
            String jwt = getJwtFromRequest(request);
            if (StringUtils.hasText(jwt) && validateToken(jwt)) {
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
        catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
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
        } catch (JWTVerificationException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
            return false;
        }
    }
}
