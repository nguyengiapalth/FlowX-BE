package project.ii.flowx.module.content.dto.reaction;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import project.ii.flowx.applications.enums.ReactionType;

import java.util.Map;
import java.util.UUID;

@Schema(description = "Reaction Summary")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReactionSummary {
    
    @Schema(description = "ID of the content (post or comment)")
    UUID contentId;
    
    @Schema(description = "Total number of reactions")
    Long totalReactions;
    
    @Schema(description = "Count of reactions by type")
    Map<ReactionType, Long> reactionCounts;
    
    @Schema(description = "Current user's reaction type (null if not reacted)")
    ReactionType userReactionType;
} 