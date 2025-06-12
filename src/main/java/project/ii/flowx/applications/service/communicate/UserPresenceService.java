package project.ii.flowx.applications.service.communicate;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import project.ii.flowx.applications.service.helper.EntityLookupService;
import project.ii.flowx.model.dto.user.UserPresenceEvent;
import project.ii.flowx.model.entity.User;

import java.time.Duration;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserPresenceService {
    StringRedisTemplate redisTemplate;
    SimpMessagingTemplate messagingTemplate;
    EntityLookupService entityLookupService;
    
    private static final String USER_ONLINE_KEY_PREFIX = "user:online:";
    private static final String ONLINE_USERS_SET = "users:online";
    private static final int ONLINE_EXPIRY_SECONDS = 300; // 5 minutes
    
    public void markUserOnline(Long userId) {
        try {
            String userKey = USER_ONLINE_KEY_PREFIX + userId;
            
            // Set user as online with expiry
            redisTemplate.opsForValue().set(userKey, 
                String.valueOf(System.currentTimeMillis()), 
                Duration.ofSeconds(ONLINE_EXPIRY_SECONDS));
            
            // Add to online users set
            redisTemplate.opsForSet().add(ONLINE_USERS_SET, userId.toString());
            
            // Broadcast to all users
            broadcastPresenceUpdate(userId, "ONLINE");
            
            log.debug("User {} marked as online", userId);
        } catch (Exception e) {
            log.error("Error marking user {} as online: {}", userId, e.getMessage());
        }
    }
    
    public void markUserOffline(Long userId) {
        try {
            String userKey = USER_ONLINE_KEY_PREFIX + userId;
            
            // Remove from Redis
            redisTemplate.delete(userKey);
            redisTemplate.opsForSet().remove(ONLINE_USERS_SET, userId.toString());
            
            // Broadcast to all users
            broadcastPresenceUpdate(userId, "OFFLINE");
            
            log.debug("User {} marked as offline", userId);
        } catch (Exception e) {
            log.error("Error marking user {} as offline: {}", userId, e.getMessage());
        }
    }
    
    public Set<String> getOnlineUsers() {
        try {
            return redisTemplate.opsForSet().members(ONLINE_USERS_SET);
        } catch (Exception e) {
            log.error("Error getting online users: {}", e.getMessage());
            return Set.of();
        }
    }
    
    public boolean isUserOnline(Long userId) {
        try {
            return redisTemplate.hasKey(USER_ONLINE_KEY_PREFIX + userId);
        } catch (Exception e) {
            log.error("Error checking if user {} is online: {}", userId, e.getMessage());
            return false;
        }
    }
    
    private void broadcastPresenceUpdate(Long userId, String status) {
        try {
            // Get user details for richer presence event
            User user = entityLookupService.getUserById(userId);
            
            UserPresenceEvent event = UserPresenceEvent.builder()
                .userId(userId)
                .status(status)
                .timestamp(System.currentTimeMillis())
                .userEmail(user.getEmail())
                .userName(user.getFullName() != null ? user.getFullName() : user.getEmail())
                .build();
                
            messagingTemplate.convertAndSend("/topic/user.presence", event);
            
            log.debug("Broadcasted presence update for user {}: {}", userId, status);
        } catch (Exception e) {
            log.error("Error broadcasting presence update for user {}: {}", userId, e.getMessage());
        }
    }
    
    // Heartbeat để refresh expiry
    public void refreshUserPresence(Long userId) {
        if (isUserOnline(userId)) {
            markUserOnline(userId); // Reset expiry
        }
    }
    
    // Cleanup method để remove expired users from set
    public void cleanupExpiredUsers() {
        try {
            Set<String> onlineUserIds = getOnlineUsers();
            for (String userIdStr : onlineUserIds) {
                Long userId = Long.parseLong(userIdStr);
                if (!isUserOnline(userId)) {
                    // User expired but still in set, remove them
                    redisTemplate.opsForSet().remove(ONLINE_USERS_SET, userIdStr);
                    broadcastPresenceUpdate(userId, "OFFLINE");
                }
            }
        } catch (Exception e) {
            log.error("Error cleaning up expired users: {}", e.getMessage());
        }
    }
} 