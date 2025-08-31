package project.ii.flowx.module.notify.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Notification Response")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponse {
    UUID id;
    String title;
    String content;
    NotificationTarget target;
    Boolean isRead;
    LocalDateTime createdAt;
    LocalDateTime readAt;
}

