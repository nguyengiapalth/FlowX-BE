package project.ii.flowx.module.manage.dto.projectmember;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import project.ii.flowx.module.manage.dto.project.ProjectResponse;
import project.ii.flowx.applications.enums.RoleDefault;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Project Member Response")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectMemberResponse {
    UUID id;
    ProjectResponse project;
    
    @Schema(description = "ID of the user who is a member")
    UUID userId;
    
    RoleDefault role;
    LocalDate joinDate;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}