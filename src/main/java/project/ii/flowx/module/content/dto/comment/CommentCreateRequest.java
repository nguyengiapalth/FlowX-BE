package project.ii.flowx.module.content.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Schema(description = "Comment Create Request")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class CommentCreateRequest {
    @NotBlank(message = "Comment body is required")
    @Size(min = 1, max = 1000, message = "Comment body must be between 1 and 1000 characters")
    @Schema(description = "Body or content of the comment", example = "This is my comment on the post")
    String body;

    @NotNull(message = "Post ID is required")
    @Schema(description = "ID of the post this comment belongs to", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID postId;

    @Schema(description = "ID of the parent comment if this is a reply, null for top-level comments", example = "null")
    UUID parentId;
} 