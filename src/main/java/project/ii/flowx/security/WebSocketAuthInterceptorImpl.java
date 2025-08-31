package project.ii.flowx.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

/**
 * Interceptor for WebSocket connections that authenticates users based on JWT tokens.
 * It extracts the JWT from the STOMP headers, decodes it, and sets the user in the security context.
 * Why custom interceptor?
 * Socket connections do not go through the standard HTTP security filters, dont have bearer token in the request header.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthInterceptorImpl implements ChannelInterceptor {

    private final JwtDecoder jwtDecoder;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    public Message<?> preSend(@NotNull Message<?> message, @NotNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authorizationHeader = accessor.getFirstNativeHeader("Authorization");
            
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7);
                try {
                    Jwt jwt = jwtDecoder.decode(token);
                    String email = jwt.getSubject();
                    
                    UserPrincipal userPrincipal = (UserPrincipal) userDetailsService.loadUserByUsername(email);
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
                    
                    accessor.setUser(authentication);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    log.debug("WebSocket authenticated user: {} (ID: {})", email, userPrincipal.getId());
                } catch (JwtException e) {
                    log.warn("Invalid JWT token in WebSocket connection: {}", e.getMessage());
                }
            }
        }
        return message;
    }
}