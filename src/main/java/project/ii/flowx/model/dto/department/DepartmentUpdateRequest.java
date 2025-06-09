package project.ii.flowx.model.dto.department;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Schema(description = "Department Update Request")
public class DepartmentUpdateRequest {
    @Schema(description = "Name of the department", example = "Engineering")
    String name;

    @Schema(description = "Description of the department", example = "Department responsible for software development")
    String description;

    @Schema(description = "Background image URL of the department", example = "https://example.com/dept-bg.jpg")
    String background;
}