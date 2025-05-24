package project.ii.flowx.model.dto.useractivitylog;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Schema(description = "User Activity Log Create Request")
@Data
@Getter
@Setter
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class UserActivityLogCreateRequest {
    @Schema(description = "ID of the user", example = "1")
    Long userId;

    @Schema(description = "Action performed by the user", example = "LOGIN")
    String action;

    @Schema(description = "Type of the entity involved", example = "USER")
    String entityType;

    @Schema(description = "ID of the entity involved", example = "1")
    Long entityId;

    @Schema(description = "Additional details about the activity", example = "User logged in from mobile device")
    String details;

    @Schema(description = "IP address of the user", example = "192.168.1.1")
    String ipAddress;

    @Schema(description = "User agent of the browser or application", example = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
    String userAgent;
}
