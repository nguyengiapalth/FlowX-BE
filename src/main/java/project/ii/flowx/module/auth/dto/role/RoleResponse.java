package project.ii.flowx.module.auth.dto.role;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Role Response")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleResponse {
    @Schema(description = "Role ID", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID id;
    
    @Schema(description = "Role Name", example = "Admin")
    String name;
    
    @Schema(description = "Role Description", example = "Administrator role with all permissions")
    String description;
    
    @Schema(description = "Created At", example = "2023-01-01T00:00:00Z")
    LocalDateTime createdAt;
    
    @Schema(description = "Updated At", example = "2023-01-02T00:00:00Z")
    LocalDateTime updatedAt;
}