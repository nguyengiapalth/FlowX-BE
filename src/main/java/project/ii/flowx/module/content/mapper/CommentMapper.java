package project.ii.flowx.module.content.mapper;

import org.mapstruct.*;
import project.ii.flowx.module.content.dto.comment.CommentCreateRequest;
import project.ii.flowx.module.content.dto.comment.CommentResponse;
import project.ii.flowx.module.content.dto.comment.CommentUpdateRequest;
import project.ii.flowx.module.content.entity.Comment;

import java.util.List;
import java.util.UUID;

/**
 * Mapper interface for converting between Comment entity and Comment DTOs.
 * This interface uses MapStruct to generate the implementation at compile time.
 */
@Mapper(componentModel = "spring")
public interface CommentMapper {

    /**
     * Convert CommentCreateRequest to Comment entity
     * Ignores all system-managed and relationship fields
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "authorId", ignore = true) // Will be set by service
    @Mapping(target = "post", ignore = true) // Will be set by service
    @Mapping(target = "parent", ignore = true) // Will be set by service
    @Mapping(target = "replies", ignore = true)
    @Mapping(target = "reactions", ignore = true)
    @Mapping(target = "hasFile", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "depth", ignore = true) // Will be calculated by service
    Comment toComment(CommentCreateRequest request);

    /**
     * Convert Comment entity to CommentResponse DTO
     * Maps nested objects using custom methods for better performance
     */
    @Mapping(target = "postId", source = "post.id")
    @Mapping(target = "parentId", source = "parent", qualifiedByName = "commentToParentId")
    @Mapping(target = "replies", source = "replies")
    @Mapping(target = "files", ignore = true) // Will be populated by service
    CommentResponse toCommentResponse(Comment comment);

    /**
     * Update Comment entity from CommentUpdateRequest
     * Only updates modifiable fields
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "authorId", ignore = true)
    @Mapping(target = "post", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "replies", ignore = true)
    @Mapping(target = "reactions", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "depth", ignore = true)
    void updateCommentFromRequest(@MappingTarget Comment comment, CommentUpdateRequest request);

    /**
     * Convert list of Comment entities to list of CommentResponse DTOs
     */
    List<CommentResponse> toCommentResponseList(List<Comment> comments);

    /**
     * Helper method to extract parent comment ID
     */
    @Named("commentToParentId")
    default UUID commentToParentId(Comment parentComment) {
        return parentComment != null ? parentComment.getId() : null;
    }
} 