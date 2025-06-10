package project.ii.flowx.model.dto.userrole;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import project.ii.flowx.model.dto.role.RoleResponse;
import project.ii.flowx.model.dto.user.UserResponse;
import project.ii.flowx.shared.enums.RoleScope;

@Schema(description = "User Role Response")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRoleResponse {
    Long id;
    UserResponse user;
    RoleResponse role;
    RoleScope scope;
    Long scopeId;
}