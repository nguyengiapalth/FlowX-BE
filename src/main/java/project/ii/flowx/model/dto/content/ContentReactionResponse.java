package project.ii.flowx.model.dto.content;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import project.ii.flowx.model.dto.user.UserResponse;
import project.ii.flowx.shared.enums.ReactionType;

import java.time.Instant;

@Schema(description = "Content Reaction Response")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContentReactionResponse {
    @Schema(description = "Reaction ID", example = "1")
    Long id;
    
    @Schema(description = "Content ID", example = "1")
    Long contentId;
    
    @Schema(description = "User who made the reaction")
    UserResponse user;
    
    @Schema(description = "Type of reaction", example = "LIKE")
    ReactionType reactionType;
    
    @Schema(description = "Created timestamp")
    Instant createdAt;
    
    @Schema(description = "Updated timestamp")
    Instant updatedAt;
} 