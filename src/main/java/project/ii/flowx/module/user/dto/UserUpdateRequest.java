package project.ii.flowx.module.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import project.ii.flowx.applications.enums.UserStatus;

import java.time.LocalDate;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Schema(description = "User Update Request")
public class UserUpdateRequest {

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number should be valid")
    @Schema(description = "Phone number of the user", example = "+1234567890")
    String phoneNumber;

    @Size(max = 255, message = "Address cannot exceed 255 characters")
    @Schema(description = "Address of the user", example = "123 Main St, City, Country")
    String address;

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

    @Schema(description = "Status of the user", example = "ACTIVE")
    UserStatus status;

    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    @Schema(description = "Full name of the user", example = "John Doe")
    private String fullName;

    @Past(message = "Date of birth must be in the past")
    @Schema(description = "Date of birth of the user", example = "1990-01-01")
    private LocalDate dateOfBirth;

    @Pattern(regexp = "^(MALE|FEMALE|OTHER)$", message = "Gender must be MALE, FEMALE, or OTHER")
    @Schema(description = "Gender of the user", example = "MALE")
    private String gender;
}