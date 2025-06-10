package project.ii.flowx.model.dto.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import project.ii.flowx.model.entity.User;

import java.time.LocalDateTime;

@Schema(description = "Notification Response")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponse {
    Long id;
    User user;
    String title;
    String content;
    String entityType;
    Long entityId;
    Boolean isRead;
    LocalDateTime createdAt;
    LocalDateTime readAt;
}