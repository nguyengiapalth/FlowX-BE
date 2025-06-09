package project.ii.flowx.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import project.ii.flowx.shared.enums.UserStatus;

import java.time.LocalDate;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Schema(description = "User Update Request")
public class UserUpdateRequest {

    @Schema(description = "Avatar URL of the user", example = "https://example.com/avatar.jpg")
    String avatar;

    @Schema(description = "Background image URL of the user", example = "https://example.com/background.jpg")
    String background;

    @Schema(description = "Phone number of the user", example = "+1234567890")
    String phoneNumber;

    @Schema(description = "Address of the user", example = "123 Main St, City, Country")
    String address;

    @Schema(description = "Position of the user", example = "Software Engineer")
    String position;

    @Schema(description = "Status of the user", example = "ACTIVE")
    UserStatus status;

    @Schema(description = "Full name of the user", example = "John Doe")
    private String fullName;

    @Schema(description = "Date of birth of the user", example = "1990-01-01")
    private LocalDate dateOfBirth;
}