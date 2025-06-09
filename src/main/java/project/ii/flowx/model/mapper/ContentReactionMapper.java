package project.ii.flowx.model.mapper;

import org.mapstruct.*;
import project.ii.flowx.model.dto.content.ContentReactionRequest;
import project.ii.flowx.model.dto.content.ContentReactionResponse;
import project.ii.flowx.model.entity.Content;
import project.ii.flowx.model.entity.ContentReaction;
import project.ii.flowx.model.entity.User;

import java.util.List;

/**
 * Mapper interface for converting between ContentReaction entity and ContentReaction DTOs.
 * This interface uses MapStruct to generate the implementation at compile time.
 */
@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface ContentReactionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "content", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ContentReaction toContentReaction(ContentReactionRequest request);

    @Mapping(target = "contentId", source = "content.id")
    @Mapping(target = "user", source = "user")
    ContentReactionResponse toContentReactionResponse(ContentReaction contentReaction);

    List<ContentReactionResponse> toContentReactionResponseList(List<ContentReaction> contentReactions);
}