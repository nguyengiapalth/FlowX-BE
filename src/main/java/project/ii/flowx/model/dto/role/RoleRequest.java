package project.ii.flowx.model.dto.role;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Schema(description = "Role Create or Update Request")
public class RoleRequest {
    @Schema(description = "Role Name", example = "Admin")
    String name;
    @Schema(description = "Role Description", example = "Administrator role with all permissions")
    String description;
}
