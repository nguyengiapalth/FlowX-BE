package project.ii.flowx.model.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import project.ii.flowx.shared.enums.EntityType;
import project.ii.flowx.shared.enums.FileVisibility;

@Schema(description = "File Create Request")
@Data
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

    @Schema(description = "ID of the uploader (User)", example = "1")
    Long uploaderId;

    @Schema(description = "ID of the related entity", example = "5")
    Long entityId;

    @Schema(description = "Type of the related entity", example = "CONTENT")
    EntityType entityType;

    @Schema(description = "Visibility of the file", example = "PRIVATE")
    FileVisibility visibility;

    public Long getUploaderId() {
        return uploaderId;
    }

    public void setUploaderId(Long uploaderId) {
        this.uploaderId = uploaderId;
    }
}
