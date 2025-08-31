package project.ii.flowx.module.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPresenceEvent {
    private UUID userId;
    private String status; // ONLINE, OFFLINE
    private Long timestamp;
    private String userEmail;
    private String userName;
} 