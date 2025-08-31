package project.ii.flowx.module.content.mapper;

import org.mapstruct.*;
import project.ii.flowx.module.content.dto.post.PostCreateRequest;
import project.ii.flowx.module.content.dto.post.PostResponse;
import project.ii.flowx.module.content.dto.post.PostUpdateRequest;
import project.ii.flowx.module.content.entity.Post;

import java.util.List;

/**
 * Mapper interface for converting between Post entity and Post DTOs.
 * This interface uses MapStruct to generate the implementation at compile time.
 */
@Mapper(componentModel = "spring")
public interface PostMapper {

    /**
     * Convert PostCreateRequest to Post entity
     * Ignores all system-managed and relationship fields
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "authorId", ignore = true) // Will be set by service
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "reactions", ignore = true)
    @Mapping(target = "hasFile", ignore = true) // Will be updated when files are uploaded
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Post toPost(PostCreateRequest request);

    /**
     * Convert Post entity to PostResponse DTO
     * Collections will be populated separately in service for better performance
     */
    @Mapping(target = "comments", ignore = true) // Will be populated separately in service
    @Mapping(target = "files", ignore = true) // Will be populated separately in service
    PostResponse toPostResponse(Post post);

    /**
     * Update Post entity from PostUpdateRequest
     * Only updates modifiable fields, ignores system-managed fields
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "authorId", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "reactions", ignore = true)
    @Mapping(target = "hasFile", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updatePostFromRequest(@MappingTarget Post post, PostUpdateRequest request);

    /**
     * Convert list of Post entities to list of PostResponse DTOs
     */
    List<PostResponse> toPostResponseList(List<Post> posts);
} 