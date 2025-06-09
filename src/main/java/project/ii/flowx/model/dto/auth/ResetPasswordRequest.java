package project.ii.flowx.model.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ResetPasswordRequest {
    @Schema(description = "Token reset password", example = "abc123xyz")
    String token;
    
    @Schema(description = "Mật khẩu mới", example = "newPassword123")
    String newPassword;
} 