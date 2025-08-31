package project.ii.flowx.module.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import project.ii.flowx.applications.enums.UserStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "User Response")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    UUID id;
    String email;
    String fullName;
    String avatar;
    String background;

    String phoneNumber;
    LocalDate dateOfBirth;
    String address;

    String position;
    String bio;
    String facebook;
    String linkedin;
    String twitter;
    LocalDate joinDate;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    UserStatus status;
    String gender;
}
