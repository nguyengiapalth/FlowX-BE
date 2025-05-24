package project.ii.flowx.model.dto.projectmember;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import project.ii.flowx.shared.enums.MemberStatus;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Schema(description = "Project Member Update Request")
public class ProjectMemberUpdateRequest {
    @Schema(description = "Role of the user in the project, MANAGER or MEMBER", example = "MANAGER")
    String role;

    @Schema(description = "Status of the member in the project", example = "INACTIVE")
    MemberStatus status;
}