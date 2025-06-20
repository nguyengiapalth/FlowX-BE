package project.ii.flowx.model.dto.useractivitylog;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import project.ii.flowx.model.entity.User;

import java.time.LocalDateTime;

@Schema(description = "User Activity Log Response")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserActivityLogResponse {
    Long id;
    User user;
    String action;
    String entityType;
    Long entityId;
    String details;
    String ipAddress;
    String userAgent;
    LocalDateTime createdAt;
}
