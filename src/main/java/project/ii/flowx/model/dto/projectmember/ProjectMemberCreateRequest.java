package project.ii.flowx.model.dto.projectmember;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import project.ii.flowx.shared.enums.MemberStatus;
import project.ii.flowx.shared.enums.RoleDefault;

@Schema(description = "Project Member Create Request")
@Data
@Getter
@Setter
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ProjectMemberCreateRequest {
    @Schema(description = "ID of the project", example = "1")
    Long projectId;

    @Schema(description = "ID of the user", example = "2")
    Long userId;

    @Schema(description = "Role of the user in the project, MANAGER or MEMBER", example = "MEMBER")
    RoleDefault role;

    @Schema(description = "Status of the member in the project", example = "ACTIVE")
    MemberStatus status;
}
