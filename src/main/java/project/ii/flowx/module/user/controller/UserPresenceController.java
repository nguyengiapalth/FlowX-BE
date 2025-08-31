package project.ii.flowx.module.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import project.ii.flowx.module.user.service.UserPresenceService;
import project.ii.flowx.security.UserPrincipal;

import java.security.Principal;
import java.util.Set;
import java.util.UUID;

/**
 * Controller to manage user presence in WebSocket connections.
 * Handles user connection, disconnection, and heartbeat events.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class UserPresenceController {
    private final UserPresenceService presenceService;
    private final SimpMessagingTemplate messagingTemplate;
    private final SimpUserRegistry simpUserRegistry;
    
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        try {
            StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
            
            if (headerAccessor.getUser() instanceof UsernamePasswordAuthenticationToken auth) {
                UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
                UUID userId = principal.getId();
                
                // Mark user as online
                presenceService.markUserOnline(userId);
                
                // Store userId in session for cleanup
                if (headerAccessor.getSessionAttributes() != null) {
                    headerAccessor.getSessionAttributes().put("userId", userId);
                }
                
                // Send current online users to the newly connected user
                Set<String> onlineUsers = presenceService.getOnlineUsers();
                messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/online-users",
                    onlineUsers
                );
                
                log.info("User {} connected to WebSocket and marked as online. Sent {} online users list.", 
                    userId, onlineUsers.size());
            }
        } catch (Exception e) {
            log.error("Error handling WebSocket connect event: {}", e.getMessage());
        }
    }
    
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        try {
            StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
            
            if (headerAccessor.getSessionAttributes() != null) {
                UUID userId = (UUID) headerAccessor.getSessionAttributes().get("userId");
                
                if (userId != null) {
                    presenceService.markUserOffline(userId);
                    log.info("User {} disconnected from WebSocket and marked as offline", userId);
                }
            }
        } catch (Exception e) {
            log.error("Error handling WebSocket disconnect event: {}", e.getMessage());
        }
    }
    
    // Heartbeat endpoint to keep user presence alive
    @MessageMapping("/user.heartbeat")
    public void handleHeartbeat(Principal principal) {
        try {
            if (principal instanceof UsernamePasswordAuthenticationToken auth) {
                UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
                UUID userId = userPrincipal.getId();
                
                presenceService.refreshUserPresence(userId);
                log.debug("Heartbeat received from user {}", userId);
            }
        } catch (Exception e) {
            log.error("Error handling heartbeat: {}", e.getMessage());
        }
    }
    
    // Send current online users to requesting user
    @MessageMapping("/user.getOnlineUsers")
    public void getOnlineUsers(Principal principal) {
        try {
            if (principal instanceof UsernamePasswordAuthenticationToken auth) {
                UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
                UUID userId = userPrincipal.getId();
                
                Set<String> onlineUsers = presenceService.getOnlineUsers();
                messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/online-users",
                    onlineUsers
                );
                
                log.debug("Sent current online users list ({}) to user {}", onlineUsers.size(), userId);
            }
        } catch (Exception e) {
            log.error("Error handling getOnlineUsers request: {}", e.getMessage());
        }
    }
} 