package project.ii.flowx.module.content.dto.reaction;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import project.ii.flowx.applications.enums.ReactionType;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Reaction Response")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReactionResponse {
    
    @Schema(description = "Reaction ID")
    UUID id;
    
    @Schema(description = "ID of the post (if reaction is on a post)")
    UUID postId;
    
    @Schema(description = "ID of the comment (if reaction is on a comment)")
    UUID commentId;
    
    @Schema(description = "ID of the user who made the reaction")
    UUID userId;
    
    @Schema(description = "Type of reaction")
    ReactionType reactionType;
    
    @Schema(description = "When the reaction was created")
    LocalDateTime createdAt;
    
    @Schema(description = "When the reaction was last updated")
    LocalDateTime updatedAt;
} 