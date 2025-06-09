package project.ii.flowx.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Schema(description = "User Avatar/Background Update Request")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAvatarUpdateRequest {
    @Schema(description = "Avatar URL of the user", example = "https://example.com/avatar.jpg")
    String avatar;

    @Schema(description = "Background image URL of the user", example = "https://example.com/background.jpg")
    String background;
} 