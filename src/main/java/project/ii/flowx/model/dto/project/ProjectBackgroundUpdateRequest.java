package project.ii.flowx.model.dto.project;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Schema(description = "Project Background Update Request")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectBackgroundUpdateRequest {
    @Schema(description = "Background image URL of the project", example = "https://example.com/project-bg.jpg")
    String background;
} 