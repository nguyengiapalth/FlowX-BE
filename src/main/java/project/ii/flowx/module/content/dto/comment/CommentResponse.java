package project.ii.flowx.module.content.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import project.ii.flowx.module.file.dto.FileResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Comment Response")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponse {
    UUID id;
    String body;
    
    @Schema(description = "ID of the comment author")
    UUID authorId;
    
    UUID postId;
    UUID parentId;
    int depth;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    
    @Schema(description = "List of replies to this comment")
    List<CommentResponse> replies;
    
    @Schema(description = "Indicates if comment has attached files")
    boolean hasFile;
    
    @Schema(description = "List of attached files")
    List<FileResponse> files;
} 