package project.ii.flowx.module.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import project.ii.flowx.applications.enums.UserStatus;

import java.time.LocalDate;

@Schema(description = "User Create Request")
@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class UserCreateRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Schema(description = "Email of the user", example = "nguyengiapnf5@gmail.com")
    String email;

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    @Schema(description = "Full name of the user", example = "John Doe")
    String fullName;

    @Schema(description = "Avatar URL of the user", example = "https://example.com/avatar.jpg")
    String avatar;

    @Schema(description = "Background image URL of the user", example = "https://example.com/background.jpg")
    String background;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number should be valid")
    @Schema(description = "Phone number of the user", example = "+1234567890")
    String phoneNumber;

    @Past(message = "Date of birth must be in the past")
    @Schema(description = "Date of birth of the user", example = "1990-01-01")
    LocalDate dateOfBirth;

    @Size(max = 255, message = "Address cannot exceed 255 characters")
    @Schema(description = "Address of the user", example = "123 Main St, City, Country")
    String address;

    @Size(max = 100, message = "Position cannot exceed 100 characters")
    @Schema(description = "Position of the user", example = "Software Engineer")
    String position;

    @Size(max = 500, message = "Bio cannot exceed 500 characters")
    @Schema(description = "Bio of the user", example = "I am a software engineer with 5 years of experience")
    String bio;

    @Pattern(regexp = "^https?://(www\\.)?facebook\\.com/.+", message = "Invalid Facebook URL format")
    @Schema(description = "Facebook URL of the user", example = "https://facebook.com/johndoe")
    String facebook;

    @Pattern(regexp = "^https?://(www\\.)?linkedin\\.com/.+", message = "Invalid LinkedIn URL format")
    @Schema(description = "LinkedIn URL of the user", example = "https://linkedin.com/in/johndoe")
    String linkedin;

    @Pattern(regexp = "^https?://(www\\.)?twitter\\.com/.+", message = "Invalid Twitter URL format")
    @Schema(description = "Twitter URL of the user", example = "https://twitter.com/johndoe")
    String twitter;

    @Enumerated(EnumType.STRING)
    @Schema(description = "Status of the user", example = "ACTIVE")
    UserStatus status;

    @Pattern(regexp = "^(MALE|FEMALE|OTHER)$", message = "Gender must be MALE, FEMALE, or OTHER")
    @Schema(description = "Gender of the user", example = "MALE")
    String gender;
}
