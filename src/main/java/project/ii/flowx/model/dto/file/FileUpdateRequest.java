package project.ii.flowx.model.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import project.ii.flowx.shared.enums.FileVisibility;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Schema(description = "File Update Request")
public class FileUpdateRequest {
    @Schema(description = "Name of the file", example = "updated-document.pdf")
    String name;

    @Schema(description = "Description of the file", example = "Updated project documentation")
    String description;

    @Schema(description = "Visibility of the file", example = "PUBLIC")
    FileVisibility visibility;
}