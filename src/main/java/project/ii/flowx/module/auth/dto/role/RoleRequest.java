package project.ii.flowx.module.auth.dto.role;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Schema(description = "Role Create or Update Request")
public class RoleRequest {
    @NotBlank(message = "Role name is required")
    @Size(min = 2, max = 50, message = "Role name must be between 2 and 50 characters")
    @Schema(description = "Role Name", example = "Admin")
    String name;
    
    @Size(max = 255, message = "Description cannot exceed 255 characters")
    @Schema(description = "Role Description", example = "Administrator role with all permissions")
    String description;
}
