package project.ii.flowx.module.content.dto.reaction;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import project.ii.flowx.applications.enums.ReactionType;

import java.util.UUID;

@Schema(description = "Reaction Create Request")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ReactionCreateRequest {
    
    @Schema(description = "ID of the post to react to (optional if reacting to comment)")
    UUID postId;
    
    @Schema(description = "ID of the comment to react to (optional if reacting to post)")
    UUID commentId;
    
    @NotNull(message = "Reaction type is required")
    @Schema(description = "Type of reaction", example = "LIKE")
    ReactionType reactionType;
} 