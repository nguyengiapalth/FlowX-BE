package project.ii.flowx.model.dto.projectmember;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import project.ii.flowx.model.dto.project.ProjectResponse;
import project.ii.flowx.model.dto.user.UserResponse;
import project.ii.flowx.shared.enums.MemberStatus;

import java.time.Instant;
import java.time.LocalDate;

@Schema(description = "Project Member Response")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectMemberResponse {
    Long id;
    ProjectResponse project;
    UserResponse user;
    String role;
    LocalDate joinDate;
    Instant createdAt;
    Instant updatedAt;
    MemberStatus status;
}