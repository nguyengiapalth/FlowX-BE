package project.ii.flowx.module.auth.dto.userrole;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import project.ii.flowx.applications.enums.RoleScope;

import java.util.UUID;

@Schema(description = "User Role Create Request")
@Data
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class UserRoleCreateRequest {
    @NotNull(message = "User ID is required")
    @Schema(description = "ID of the user", example = "1")
    UUID userId;

    @NotNull(message = "Role ID is required")
    @Schema(description = "ID of the role", example = "2")
    UUID roleId;

    @NotNull(message = "Role scope is required")
    @Enumerated(EnumType.STRING)
    @Schema(description = "Scope of the role (e.g., 'PROJECT')", example = "PROJECT")
    RoleScope scope;

    @Schema(description = "ID of the scope entity", example = "3")
    UUID scopeId;
}
