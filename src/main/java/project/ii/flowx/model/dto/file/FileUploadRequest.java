package project.ii.flowx.model.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;
import project.ii.flowx.shared.enums.FileTargetType;
import project.ii.flowx.shared.enums.FileVisibility;

@Schema(description = "File Upload Request from Controller")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class FileUploadRequest {

    @Schema(description = "File to upload", required = true)
    MultipartFile file;

    @Schema(description = "Entity type", required = true, example = "TASK")
    FileTargetType fileTargetType;

    @Schema(description = "Entity ID", required = true, example = "123")
    Long entityId;

    @Schema(description = "File description", example = "Task attachment")
    String description;

    @Schema(description = "File visibility", example = "PROJECT")
    FileVisibility visibility;
} 