package project.ii.flowx.model.dto.content;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import project.ii.flowx.shared.enums.ReactionType;

import java.util.Map;

@Schema(description = "Content Reaction Summary")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContentReactionSummary {
    @Schema(description = "Content ID", example = "1")
    Long contentId;
    
    @Schema(description = "Total reactions count", example = "25")
    Long totalReactions;
    
    @Schema(description = "Reactions count by type")
    Map<ReactionType, Long> reactionCounts;
    
    @Schema(description = "Current user's reaction (if any)", example = "LIKE")
    ReactionType userReaction;
} 