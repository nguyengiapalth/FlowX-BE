package project.ii.flowx.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import project.ii.flowx.shared.enums.UserStatus;

import java.time.Instant;
import java.time.LocalDate;

@Schema(description = "User Response")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    Long id;
    String email;
    String fullName;
    String avatar;

    String phoneNumber;
    LocalDate dateOfBirth;
    String address;

    String position;
    LocalDate joinDate;
//    DepartmentResponse department;
    Long departmentId;

    Instant createdAt;
    Instant updatedAt;
    UserStatus status;
}
