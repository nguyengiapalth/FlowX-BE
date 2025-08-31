package project.ii.flowx.module.auth.dto.auth;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * Response DTO for token session information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TokenSessionResponse {
    
    String sessionId;
    String userAgent;
    String ipAddress;
    LocalDateTime createdAt;
    LocalDateTime lastActiveAt;
    boolean current; // Is this the current session
} 