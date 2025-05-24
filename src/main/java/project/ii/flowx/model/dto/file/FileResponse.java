package project.ii.flowx.model.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import project.ii.flowx.model.entity.User;
import project.ii.flowx.shared.enums.EntityType;
import project.ii.flowx.shared.enums.FileVisibility;

import java.time.Instant;

@Schema(description = "File Response")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileResponse {
    Long id;
    String name;
    String type;
    Long size;
    String bucket;
    String objectPath;
    String contentHash;
    Long entityId;
    String description;
    User uploader;
    Instant createdAt;
    EntityType entityType;
    FileVisibility visibility;
}