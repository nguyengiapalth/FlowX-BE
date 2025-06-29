package project.ii.flowx.model.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import project.ii.flowx.model.dto.user.UserResponse;
import project.ii.flowx.shared.enums.FileTargetType;
import project.ii.flowx.shared.enums.FileStatus;
import project.ii.flowx.shared.enums.FileVisibility;

import java.time.LocalDateTime;

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
    String contentHash;
    Long targetId;
    String description;
    UserResponse uploader;
    LocalDateTime createdAt;
    FileTargetType fileTargetType;
    FileVisibility visibility;
    String url;
    Long actualSize;
    FileStatus fileStatus;
}