package project.ii.flowx.module.auth.dto.userrole;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import project.ii.flowx.module.auth.dto.role.RoleResponse;
import project.ii.flowx.applications.enums.RoleScope;

import java.util.UUID;

@Schema(description = "User Role Response")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRoleResponse {
    UUID id;
    UUID userId;
    RoleResponse role;
    RoleScope scope;
    UUID scopeId;
}