package project.ii.flowx.controller.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import project.ii.flowx.applications.service.communicate.UserPresenceService;
import project.ii.flowx.model.dto.FlowXResponse;

import java.util.Set;

@RestController
@RequestMapping("/api/user-presence")
@RequiredArgsConstructor
@Slf4j
public class UserPresenceRestController {
    private final UserPresenceService presenceService;
    
    @GetMapping("/online-users")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FlowXResponse<Set<String>>> getOnlineUsers() {
        try {
            Set<String> onlineUsers = presenceService.getOnlineUsers();
            log.debug("Retrieved {} online users", onlineUsers.size());
            
            return ResponseEntity.ok(FlowXResponse.<Set<String>>builder()
                    .code(200)
                    .message("Online users retrieved successfully")
                    .data(onlineUsers)
                    .build());
        } catch (Exception e) {
            log.error("Error getting online users: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(FlowXResponse.<Set<String>>builder()
                            .code(500)
                            .message("Failed to get online users: " + e.getMessage())
                            .build());
        }
    }
    
    @GetMapping("/is-online/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FlowXResponse<Boolean>> isUserOnline(@PathVariable Long userId) {
        try {
            boolean isOnline = presenceService.isUserOnline(userId);
            log.debug("User {} is {}", userId, isOnline ? "online" : "offline");
            
            return ResponseEntity.ok(FlowXResponse.<Boolean>builder()
                    .code(200)
                    .message("User status retrieved successfully")
                    .data(isOnline)
                    .build());
        } catch (Exception e) {
            log.error("Error checking user {} online status: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(FlowXResponse.<Boolean>builder()
                            .code(500)
                            .message("Failed to check user status: " + e.getMessage())
                            .build());
        }
    }
    
    @PostMapping("/force-online/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FlowXResponse<Void>> forceUserOnline(@PathVariable Long userId) {
        try {
            presenceService.markUserOnline(userId);
            log.info("Forced user {} to be online", userId);
            
            return ResponseEntity.ok(FlowXResponse.<Void>builder()
                    .code(200)
                    .message("User marked as online successfully")
                    .build());
        } catch (Exception e) {
            log.error("Error forcing user {} online: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(FlowXResponse.<Void>builder()
                            .code(500)
                            .message("Failed to mark user as online: " + e.getMessage())
                            .build());
        }
    }
    
    @PostMapping("/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FlowXResponse<Void>> cleanupExpiredUsers() {
        try {
            presenceService.cleanupExpiredUsers();
            log.info("Manual cleanup of expired users completed");
            
            return ResponseEntity.ok(FlowXResponse.<Void>builder()
                    .code(200)
                    .message("Cleanup completed successfully")
                    .build());
        } catch (Exception e) {
            log.error("Error during manual cleanup: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(FlowXResponse.<Void>builder()
                            .code(500)
                            .message("Failed to cleanup expired users: " + e.getMessage())
                            .build());
        }
    }
} 