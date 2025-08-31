package project.ii.flowx.module.auth.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.List;

/**
 * Service for managing refresh token sessions using Redis.
 * Provides distributed locking and race condition protection for token refresh operations.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RefreshTokenSessionService {
    
    StringRedisTemplate redisTemplate;
    
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final String USER_SESSION_PREFIX = "user_session:";
    private static final String TOKEN_LOCK_PREFIX = "token_lock:";
    private static final String SESSION_DATA_PREFIX = "session_data:";
    
    private static final int LOCK_TIMEOUT_SECONDS = 10;
    private static final int MAX_SESSIONS_PER_USER = 5;
    
    /**
     * Store refresh token session metadata in Redis
     */
    public void storeRefreshTokenSession(String tokenId, UUID userId, Instant expiryTime, String userAgent, String ipAddress) {
        try {
            String tokenKey = REFRESH_TOKEN_PREFIX + tokenId;
            String userSessionKey = USER_SESSION_PREFIX + userId;
            String sessionDataKey = SESSION_DATA_PREFIX + tokenId;
            
            Duration ttl = Duration.between(Instant.now(), expiryTime);
            if (ttl.isNegative() || ttl.isZero()) {
                log.warn("Attempted to store session with past or zero expiry time: {}", expiryTime);
                return;
            }
            
            // Store token -> userId mapping
            redisTemplate.opsForValue().set(tokenKey, userId.toString(), ttl);
            
            // Store session metadata
            String sessionData = String.format("%s|%s|%d", userAgent != null ? userAgent : "unknown", 
                                               ipAddress != null ? ipAddress : "unknown", 
                                               System.currentTimeMillis());
            redisTemplate.opsForValue().set(sessionDataKey, sessionData, ttl);
            
            // Enforce session limit before adding new session
            enforceSessionLimit(userId);
            
            // Add to user's active sessions
            redisTemplate.opsForSet().add(userSessionKey, tokenId);
            redisTemplate.expire(userSessionKey, ttl);
            
            log.info("Stored refresh token session for user: {} with token ID: {}", userId, tokenId);
            
        } catch (Exception e) {
            log.error("Failed to store refresh token session for user: {} - {}", userId, e.getMessage(), e);
            throw new FlowXException(FlowXError.INTERNAL_SERVER_ERROR, "Failed to store session");
        }
    }
    
    /**
     * Validate and invalidate refresh token with distributed locking to prevent race conditions
     */
    public boolean validateAndInvalidateToken(String tokenId) {
        String lockKey = TOKEN_LOCK_PREFIX + tokenId;
        
        try {
            // Acquire distributed lock
            Boolean lockAcquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "locked", Duration.ofSeconds(LOCK_TIMEOUT_SECONDS));
            
            if (Boolean.FALSE.equals(lockAcquired)) {
                log.warn("Token refresh already in progress for token: {}", tokenId);
                throw new FlowXException(FlowXError.CONCURRENT_OPERATION, 
                    "Token refresh already in progress. Please try again.");
            }
            
            try {
                String tokenKey = REFRESH_TOKEN_PREFIX + tokenId;
                
                // Check if token exists and get user ID
                String userId = redisTemplate.opsForValue().get(tokenKey);
                if (userId == null) {
                    log.warn("Refresh token not found or expired: {}", tokenId);
                    return false; // Token không tồn tại hoặc đã expired
                }
                
                // Immediately invalidate token to prevent reuse
                redisTemplate.delete(tokenKey);
                
                // Remove from user's session set
                String userSessionKey = USER_SESSION_PREFIX + userId;
                redisTemplate.opsForSet().remove(userSessionKey, tokenId);
                
                // Remove session metadata
                String sessionDataKey = SESSION_DATA_PREFIX + tokenId;
                redisTemplate.delete(sessionDataKey);
                
                log.info("Successfully validated and invalidated token: {} for user: {}", tokenId, userId);
                return true;
                
            } finally {
                // Always release the lock
                redisTemplate.delete(lockKey);
            }
            
        } catch (FlowXException e) {
            throw e; // Re-throw FlowX exceptions
        } catch (Exception e) {
            log.error("Error during token validation for token: {} - {}", tokenId, e.getMessage(), e);
            throw new FlowXException(FlowXError.INTERNAL_SERVER_ERROR, "Token validation failed");
        }
    }
    
    /**
     * Get user ID from refresh token
     */
    public UUID getUserIdFromToken(String tokenId) {
        try {
            String tokenKey = REFRESH_TOKEN_PREFIX + tokenId;
            String userId = redisTemplate.opsForValue().get(tokenKey);
            return userId != null ? UUID.fromString(userId) : null;
        } catch (Exception e) {
            log.error("Error getting user ID from token: {} - {}", tokenId, e.getMessage());
            return null;
        }
    }
    
    /**
     * Revoke all refresh token sessions for a user (logout from all devices)
     */
    public void revokeAllUserSessions(UUID userId) {
        try {
            String userSessionKey = USER_SESSION_PREFIX + userId;
            Set<String> tokenIds = redisTemplate.opsForSet().members(userSessionKey);
            
            if (tokenIds != null && !tokenIds.isEmpty()) {
                List<String> keysToDelete = tokenIds.stream()
                    .flatMap(tokenId -> List.of(
                        REFRESH_TOKEN_PREFIX + tokenId,
                        SESSION_DATA_PREFIX + tokenId
                    ).stream())
                    .toList();
                
                redisTemplate.delete(keysToDelete);
                redisTemplate.delete(userSessionKey);
                
                log.info("Revoked {} sessions for user: {}", tokenIds.size(), userId);
            }
            
        } catch (Exception e) {
            log.error("Error revoking sessions for user: {} - {}", userId, e.getMessage(), e);
            throw new FlowXException(FlowXError.INTERNAL_SERVER_ERROR, "Failed to revoke user sessions");
        }
    }
    
    /**
     * Revoke specific refresh token session
     */
    public void revokeSession(String tokenId) {
        try {
            UUID userId = getUserIdFromToken(tokenId);
            if (userId != null) {
                // Remove token
                redisTemplate.delete(REFRESH_TOKEN_PREFIX + tokenId);
                
                // Remove from user session set
                String userSessionKey = USER_SESSION_PREFIX + userId;
                redisTemplate.opsForSet().remove(userSessionKey, tokenId);
                
                // Remove session metadata
                redisTemplate.delete(SESSION_DATA_PREFIX + tokenId);
                
                log.info("Revoked session {} for user: {}", tokenId, userId);
            }
        } catch (Exception e) {
            log.error("Error revoking session: {} - {}", tokenId, e.getMessage(), e);
        }
    }
    
    /**
     * Get count of active sessions for a user
     */
    public int getActiveSessionCount(UUID userId) {
        try {
            String userSessionKey = USER_SESSION_PREFIX + userId;
            Long count = redisTemplate.opsForSet().size(userSessionKey);
            return count != null ? count.intValue() : 0;
        } catch (Exception e) {
            log.error("Error getting session count for user: {} - {}", userId, e.getMessage());
            return 0;
        }
    }
    
    /**
     * Get active session IDs for a user
     */
    public Set<String> getUserActiveSessions(UUID userId) {
        try {
            String userSessionKey = USER_SESSION_PREFIX + userId;
            return redisTemplate.opsForSet().members(userSessionKey);
        } catch (Exception e) {
            log.error("Error getting active sessions for user: {} - {}", userId, e.getMessage());
            return Set.of();
        }
    }
    
    /**
     * Check if a token is still valid (exists in Redis)
     */
    public boolean isTokenValid(String tokenId) {
        try {
            String tokenKey = REFRESH_TOKEN_PREFIX + tokenId;
            return redisTemplate.hasKey(tokenKey);
        } catch (Exception e) {
            log.error("Error checking token validity: {} - {}", tokenId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Enforce session limit per user by removing oldest sessions
     */
    private void enforceSessionLimit(UUID userId) {
        try {
            String userSessionKey = USER_SESSION_PREFIX + userId;
            Long sessionCount = redisTemplate.opsForSet().size(userSessionKey);
            
            if (sessionCount != null && sessionCount >= MAX_SESSIONS_PER_USER) {
                // Get all sessions and remove oldest ones
                Set<String> sessions = redisTemplate.opsForSet().members(userSessionKey);
                if (sessions != null && sessions.size() >= MAX_SESSIONS_PER_USER) {
                    
                    int toRemove = sessions.size() - MAX_SESSIONS_PER_USER + 1;
                    
                    sessions.stream()
                        .limit(toRemove)
                        .forEach(tokenId -> {
                            redisTemplate.delete(REFRESH_TOKEN_PREFIX + tokenId);
                            redisTemplate.delete(SESSION_DATA_PREFIX + tokenId);
                            redisTemplate.opsForSet().remove(userSessionKey, tokenId);
                        });
                    
                    log.info("Removed {} old sessions for user: {} due to session limit", toRemove, userId);
                }
            }
        } catch (Exception e) {
            log.error("Error enforcing session limit for user: {} - {}", userId, e.getMessage());
        }
    }
    
    /**
     * Clean up expired sessions (scheduled task can call this)
     */
    public void cleanupExpiredSessions() {
        try {
            // This is handled automatically by Redis TTL, but we can add manual cleanup logic here if needed
            log.debug("Session cleanup completed - Redis TTL handles expiration automatically");
        } catch (Exception e) {
            log.error("Error during session cleanup: {}", e.getMessage(), e);
        }
    }
} 