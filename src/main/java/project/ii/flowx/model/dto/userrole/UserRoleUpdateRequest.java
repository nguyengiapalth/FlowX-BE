package project.ii.flowx.model.dto.userrole;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Schema(description = "User Role Update Request")
public class UserRoleUpdateRequest {
    @Schema(description = "ID of the role", example = "2")
    Long roleId;

    @Schema(description = "Scope of the role (e.g., 'project', 'department')", example = "department")
    String scope;
}