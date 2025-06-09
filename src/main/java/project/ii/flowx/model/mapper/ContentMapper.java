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
@Mapper(componentModel = "spring")
public interface ContentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parent", source = "parentId", qualifiedByName = "idToContent")
    @Mapping(target = "replies", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Content toContent(ContentCreateRequest request);

    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "hasFile", ignore = true)
    @Mapping(target = "files", ignore = true)
    ContentResponse toContentResponse(Content content);

    void updateContentFromRequest(@MappingTarget Content content, ContentUpdateRequest request);

    List<ContentResponse> toContentResponseList(List<Content> contents);

    @Named("idToContent")
    default Content idToContent(Long id) {
        if (id == null) return null;
        Content content = new Content();
        content.setId(id);
        return content;
    }
}