package project.ii.flowx.module.content.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Schema(description = "Post Update Request")
public class PostUpdateRequest {
    @Size(min = 1, max = 5000, message = "Post body must be between 1 and 5000 characters")
    @Schema(description = "Body or content details of the post", example = "This is the updated content of my post")
    String body;
    
    @Size(max = 200, message = "Subtitle cannot exceed 200 characters")
    @Schema(description = "Subtitle of the post", example = "This is an updated subtitle")
    String subtitle;
} 