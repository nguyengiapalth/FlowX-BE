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

    @Schema(description = "Phone number of the user", example = "+1234567890")
    String phoneNumber;

    @Schema(description = "Address of the user", example = "123 Main St, City, Country")
    String address;

    @Schema(description = "Bio of the user", example = "I am a software engineer with 5 years of experience")
    String bio;

    @Schema(description = "Facebook URL of the user", example = "https://facebook.com/johndoe")
    String facebook;

    @Schema(description = "LinkedIn URL of the user", example = "https://linkedin.com/in/johndoe")
    String linkedin;

    @Schema(description = "Twitter URL of the user", example = "https://twitter.com/johndoe")
    String twitter;

    @Schema(description = "Status of the user", example = "ACTIVE")
    UserStatus status;

    @Schema(description = "Full name of the user", example = "John Doe")
    private String fullName;

    @Schema(description = "Date of birth of the user", example = "1990-01-01")
    private LocalDate dateOfBirth;

    @Schema(description = "Gender of the user", example = "MALE")
    private String gender;
}