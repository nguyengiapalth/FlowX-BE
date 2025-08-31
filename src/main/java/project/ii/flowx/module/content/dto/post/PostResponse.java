package project.ii.flowx.module.content.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import project.ii.flowx.module.content.dto.comment.CommentResponse;
import project.ii.flowx.module.file.dto.FileResponse;
import project.ii.flowx.applications.enums.Visibility;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Post Response")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostResponse {
    UUID id;
    String body;
    
    @Schema(description = "Subtitle of the post")
    String subtitle;
    
    @Schema(description = "ID of the post author")
    UUID authorId;
    
    Visibility visibility;
    UUID targetId;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    
    @Schema(description = "List of comments on this post")
    List<CommentResponse> comments;
    
    @Schema(description = "Indicates if post has attached files")
    boolean hasFile;
    
    @Schema(description = "List of attached files")
    List<FileResponse> files;
} 