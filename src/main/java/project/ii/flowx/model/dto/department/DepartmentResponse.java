package project.ii.flowx.model.dto.department;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Schema(description = "Department Response")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentResponse {
    Long id;
    String name;
    String description;
    String background;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}