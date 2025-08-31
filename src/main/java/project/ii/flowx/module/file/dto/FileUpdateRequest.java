package project.ii.flowx.module.file.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import project.ii.flowx.applications.enums.FileVisibility;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Schema(description = "File Update Request")
public class FileUpdateRequest {
    @Size(min = 1, max = 255, message = "File name must be between 1 and 255 characters")
    @Schema(description = "Name of the file", example = "updated-document.pdf")
    String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Schema(description = "Description of the file", example = "Updated project documentation")
    String description;

    @Schema(description = "Visibility of the file", example = "PUBLIC")
    FileVisibility visibility;
}