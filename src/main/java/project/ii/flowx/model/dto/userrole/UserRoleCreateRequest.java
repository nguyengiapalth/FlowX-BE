package project.ii.flowx.model.dto.userrole;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import project.ii.flowx.shared.enums.RoleScope;

@Schema(description = "User Role Create Request")
@Data
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class UserRoleCreateRequest {
    @Schema(description = "ID of the user", example = "1")
    Long userId;

    @Schema(description = "ID of the role", example = "2")
    Long roleId;

    @Enumerated(EnumType.STRING)
    @Schema(description = "Scope of the role (e.g., 'PROJECT', 'DEPARTMENT')", example = "DEPARTMENT")
    RoleScope scope;

    @Schema(description = "ID of the scope entity", example = "3")
    Long scopeId;
}
