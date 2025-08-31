package project.ii.flowx.module.file.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import project.ii.flowx.applications.enums.FileTargetType;
import project.ii.flowx.applications.enums.FileStatus;
import project.ii.flowx.applications.enums.FileVisibility;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "File Response")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileResponse {
    UUID id;
    String name;
    String type;
    Long size;
    String contentHash;
    UUID targetId;
    String description;
    
    @Schema(description = "ID of the user who uploaded this file")
    UUID uploaderId;
    
    LocalDateTime createdAt;
    FileTargetType fileTargetType;
    FileVisibility visibility;
    String url;
    Long actualSize;
    FileStatus fileStatus;
}