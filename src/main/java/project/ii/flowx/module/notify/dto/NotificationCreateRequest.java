package project.ii.flowx.module.notify.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Schema(description = "Notification Create Request")
@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class NotificationCreateRequest {
    @NotNull(message = "User ID is required")
    @Schema(description = "ID of the user (recipient)", example = "1")
    UUID userId;

    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    @Schema(description = "Title of the notification", example = "New Message")
    String title;

    @NotBlank(message = "Content is required")
    @Size(min = 1, max = 1000, message = "Content must be between 1 and 1000 characters")
    @Schema(description = "Content of the notification", example = "You have received a new message from John Doe.")
    String content;

    @NotBlank(message = "Entity type is required")
    @Schema(description = "Type of the related entity", example = "MESSAGE")
    String entityType;

    @Schema(description = "ID of the related entity", example = "5")
    UUID targetId;

    @Valid
    @Schema(description = "Target object containing details about the notification target")
    NotificationTarget target;
}
