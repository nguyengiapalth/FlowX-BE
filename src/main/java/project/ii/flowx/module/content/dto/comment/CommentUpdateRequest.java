package project.ii.flowx.module.content.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Schema(description = "Comment Update Request")
public class CommentUpdateRequest {
    @Size(min = 1, max = 1000, message = "Comment body must be between 1 and 1000 characters")
    @Schema(description = "Body or content of the comment", example = "This is my updated comment")
    String body;
} 