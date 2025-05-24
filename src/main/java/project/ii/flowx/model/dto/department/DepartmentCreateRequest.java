package project.ii.flowx.model.dto.department;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Schema(description = "Department Create Request")
@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class DepartmentCreateRequest {
    @Schema(description = "Name of the department", example = "Engineering")
    String name;

    @Schema(description = "Description of the department", example = "Department responsible for software development")
    String description;
}