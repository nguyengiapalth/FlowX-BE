package project.ii.flowx.model.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import project.ii.flowx.shared.enums.FileTargetType;
import project.ii.flowx.shared.enums.FileVisibility;

@Schema(description = "File Create Request")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class FileCreateRequest {
    @Schema(description = "Name of the file", example = "document.pdf")
    String name;

    @Schema(description = "Type of the file", example = "application/pdf")
    String type;

    @Schema(description = "Size of the file in bytes", example = "1024")
    Long size;

    @Schema(description = "Description of the file", example = "Project documentation")
    String description;

    @Schema(description = "ID of the related entity", example = "5")
    Long entityId;

    @Schema(description = "Type of the related entity", example = "CONTENT")
    @Enumerated(EnumType.STRING)
    FileTargetType entityType;

    @Schema(description = "Visibility of the file", example = "PRIVATE")
    @Enumerated(EnumType.STRING)
    FileVisibility visibility;
}
