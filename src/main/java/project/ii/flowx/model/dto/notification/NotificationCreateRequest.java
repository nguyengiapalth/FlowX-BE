package project.ii.flowx.model.dto.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Schema(description = "Notification Create Request")
@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class NotificationCreateRequest {
    @Schema(description = "ID of the user (recipient)", example = "1")
    Long userId;

    @Schema(description = "Title of the notification", example = "New Message")
    String title;

    @Schema(description = "Content of the notification", example = "You have received a new message from John Doe.")
    String content;

    @Schema(description = "Type of the related entity", example = "MESSAGE")
    String entityType;

    @Schema(description = "ID of the related entity", example = "5")
    Long entityId;
}
