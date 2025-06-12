package project.ii.flowx.model.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPresenceEvent {
    private Long userId;
    private String status; // ONLINE, OFFLINE
    private Long timestamp;
    private String userEmail;
    private String userName;
} 