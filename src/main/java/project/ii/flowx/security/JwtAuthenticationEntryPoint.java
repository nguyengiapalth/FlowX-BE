package project.ii.flowx.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import project.ii.flowx.model.dto.FlowXResponse;

import java.io.IOException;

/**
 * Custom AuthenticationEntryPoint to handle unauthorized access attempts in a JWT-based security context.
 * This handler returns a JSON response with a 401 status code when authentication is required.
 */
@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, 
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException, ServletException {
        log.error("Unauthorized error: {}", authException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        FlowXResponse<?> errorResponse = FlowXResponse.builder()
                .code(401)
                .message("Authentication required")
                .build();
        
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
} 