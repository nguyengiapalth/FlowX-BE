package project.ii.flowx.module.content.mapper;

import org.mapstruct.*;
import project.ii.flowx.module.content.dto.reaction.ReactionResponse;
import project.ii.flowx.module.content.dto.reaction.ReactionCreateRequest;
import project.ii.flowx.module.content.entity.Reaction;

import java.util.List;

/**
 * Mapper interface for converting between Reaction entity and Reaction DTOs.
 * This interface uses MapStruct to generate the implementation at compile time.
 */
@Mapper(componentModel = "spring")
public interface ReactionMapper {

    /**
     * Convert Reaction entity to ReactionResponse DTO
     * Maps post and comment IDs from nested objects
     */
    @Mapping(target = "postId", source = "post.id")
    @Mapping(target = "commentId", source = "comment.id")
    ReactionResponse toReactionResponse(Reaction reaction);

    /**
     * Convert ReactionCreateRequest to Reaction entity
     * Ignores all system-managed and relationship fields
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true) // Will be set by service
    @Mapping(target = "post", ignore = true) // Will be set by service
    @Mapping(target = "comment", ignore = true) // Will be set by service
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Reaction toReaction(ReactionCreateRequest request);

    /**
     * Convert list of Reaction entities to list of ReactionResponse DTOs
     */
    List<ReactionResponse> toReactionResponseList(List<Reaction> reactions);
}