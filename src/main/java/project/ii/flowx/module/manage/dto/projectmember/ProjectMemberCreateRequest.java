package project.ii.flowx.module.manage.dto.projectmember;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import project.ii.flowx.applications.enums.RoleDefault;

import java.util.UUID;

@Schema(description = "Project ConversationMember Create Request")
@Data
@Getter
@Setter
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ProjectMemberCreateRequest {
    @NotNull(message = "Project ID is required")
    @Schema(description = "ID of the project", example = "1")
    UUID projectId;

    @NotNull(message = "User ID is required")
    @Schema(description = "ID of the user", example = "2")
    UUID userId;

    @NotNull(message = "Role is required")
    @Schema(description = "Role of the user in the project, MANAGER or MEMBER", example = "MEMBER")
    RoleDefault role;
}
