package project.ii.flowx.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import project.ii.flowx.shared.enums.UserStatus;

import java.time.LocalDate;

@Schema(description = "User Create Request")
@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class UserCreateRequest {
    @Schema(description = "Email of the user", example = "nguyengiapnf5@gmail.com")
    String email;

    @Schema(description = "Full name of the user", example = "John Doe")
    String fullName;

    @Schema(description = "Avatar URL of the user", example = "https://example.com/avatar.jpg")
    String avatar;

    @Schema(description = "Background image URL of the user", example = "https://example.com/background.jpg")
    String background;

    @Schema(description = "Phone number of the user", example = "+1234567890")
    String phoneNumber;

    @Schema(description = "Date of birth of the user", example = "1990-01-01")
    LocalDate dateOfBirth;

    @Schema(description = "Address of the user", example = "123 Main St, City, Country")
    String address;

    @Schema(description = "Position of the user", example = "Software Engineer")
    String position;

    @Schema(description = "Bio of the user", example = "I am a software engineer with 5 years of experience")
    String bio;

    @Schema(description = "Facebook URL of the user", example = "https://facebook.com/johndoe")
    String facebook;

    @Schema(description = "LinkedIn URL of the user", example = "https://linkedin.com/in/johndoe")
    String linkedin;

    @Schema(description = "Twitter URL of the user", example = "https://twitter.com/johndoe")
    String twitter;

    @Enumerated(EnumType.STRING)
    @Schema(description = "Status of the user", example = "ACTIVE")
    UserStatus status;
}
