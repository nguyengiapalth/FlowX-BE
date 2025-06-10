package project.ii.flowx.model.mapper;

import org.mapstruct.*;
import project.ii.flowx.model.dto.content.ContentCreateRequest;
import project.ii.flowx.model.dto.content.ContentResponse;
import project.ii.flowx.model.dto.content.ContentUpdateRequest;
import project.ii.flowx.model.entity.Content;

import java.util.List;

/**
 * Mapper interface for converting between Content entity and Content DTOs.
 * This interface uses MapStruct to generate the implementation at compile time.
 */
@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface ContentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "replies", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Content toContent(ContentCreateRequest request);

    @Mapping(target = "author", source = "author")
    @Mapping(target = "parentId", source = "parent", qualifiedByName = "contentToParentId")
    @Mapping(target = "files", ignore = true)
    ContentResponse toContentResponse(Content content);

    void updateContentFromRequest(@MappingTarget Content content, ContentUpdateRequest request);

    List<ContentResponse> toContentResponseList(List<Content> contents);

    @Named("contentToParentId")
    default long contentToParentId(Content parent) {
        if (parent == null) return -1;
        return parent.getId();
    }
}