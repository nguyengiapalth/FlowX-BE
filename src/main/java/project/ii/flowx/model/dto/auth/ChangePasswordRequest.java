package project.ii.flowx.model.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@Data
@Schema(description = "Change Password Request")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ChangePasswordRequest {
    @Schema(description = "Old Password", example = "oldPassword123")
    private String oldPassword;
    @Schema(description = "New Password", example = "newPassword123")
    private String newPassword;
}
