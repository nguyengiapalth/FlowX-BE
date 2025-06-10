package project.ii.flowx.model.dto.content;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import project.ii.flowx.model.dto.file.FileResponse;
import project.ii.flowx.model.dto.user.UserResponse;
import project.ii.flowx.shared.enums.ContentTargetType;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Content Response")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContentResponse {
    Long id;
    String body;
    
    @Schema(description = "Author of the content")
    UserResponse author;
    
    ContentTargetType contentTargetType;
    Long targetId;
    long parentId;
    int depth;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    List<ContentResponse> replies;
    
    @Schema(description = "Indicates if content has attached files")
    boolean hasFile;
    
    @Schema(description = "List of attached files")
    List<FileResponse> files;
}