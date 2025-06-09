package project.ii.flowx.model.dto.department;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Schema(description = "Department Background Update Request")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentBackgroundUpdateRequest {
    @Schema(description = "Background image URL of the department", example = "https://example.com/dept-bg.jpg")
    String background;
} 