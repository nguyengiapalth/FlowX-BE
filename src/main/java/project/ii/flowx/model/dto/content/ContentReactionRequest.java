package project.ii.flowx.model.dto.content;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import project.ii.flowx.shared.enums.ReactionType;

import jakarta.validation.constraints.NotNull;

@Schema(description = "Content Reaction Request")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContentReactionRequest {
    @NotNull(message = "Content ID is required")
    @Schema(description = "ID of the content", example = "1")
    Long contentId;

    @NotNull(message = "Reaction type is required")
    @Schema(description = "Type of reaction", example = "LIKE")
    ReactionType reactionType;
} 