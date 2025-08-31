package project.ii.flowx.module.content.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import project.ii.flowx.applications.enums.Visibility;

import java.util.UUID;

@Schema(description = "Post Create Request")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class PostCreateRequest {
    @NotBlank(message = "Post body is required")
    @Size(min = 1, max = 5000, message = "Post body must be between 1 and 5000 characters")
    @Schema(description = "Body or content details of the post", example = "This is the main content of my post")
    String body;

    @Size(max = 200, message = "Subtitle cannot exceed 200 characters")
    @Schema(description = "Subtitle of the post", example = "This is a subtitle")
    String subtitle;

    @NotNull(message = "Visibility is required")
    @Schema(description = "Type of the content target, PROJECT or GLOBAL", example = "GLOBAL")
    Visibility visibility;

    @Schema(description = "ID of the target entity (e.g., project) this post is associated with")
    UUID targetId;
} 