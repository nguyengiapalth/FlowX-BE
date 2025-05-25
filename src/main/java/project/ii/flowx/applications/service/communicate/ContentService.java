package project.ii.flowx.applications.service.communicate;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ii.flowx.model.dto.content.ContentCreateRequest;
import project.ii.flowx.model.dto.content.ContentResponse;
import project.ii.flowx.model.dto.content.ContentUpdateRequest;
import project.ii.flowx.model.entity.Content;
import project.ii.flowx.model.mapper.ContentMapper;
import project.ii.flowx.model.repository.ContentRepository;
import project.ii.flowx.applications.service.helper.EntityLookupService;
import project.ii.flowx.shared.enums.ContentTargetType;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ContentService {
    ContentRepository contentRepository;
    ContentMapper contentMapper;
    EntityLookupService entityLookupService;

    @Transactional
    public ContentResponse createContent(ContentCreateRequest request) {
        Content content = contentMapper.toContent(request);
        if (request.getParentId() != -1) {
            Content parent = entityLookupService.getContentById(request.getParentId());
            if(parent.getDepth() > 3){
                content.setDepth(parent.getDepth());
                content.setParent(parent.getParent());
            }
            else content.setDepth(parent.getDepth() + 1);
        } else {
            content.setDepth(0);
        }
        log.info("Creating content with title: {}", content.getTitle());
        content = contentRepository.save(content);
        return contentMapper.toContentResponse(content);
    }

    @Transactional
    // preauth, is create
    public ContentResponse updateContent(Long id, ContentUpdateRequest request) {
        Content content = entityLookupService.getContentById(id);
        contentMapper.updateContentFromRequest(content, request);
        content = contentRepository.save(content);
        return contentMapper.toContentResponse(content);
    }

    @Transactional
    // preauth, is create or manage in scope
    public void deleteContent(Long id) {
        Content content = entityLookupService.getContentById(id);
        contentRepository.delete(content);
    }

    @Transactional(readOnly = true)
    @PreAuthorize( "hasAuthority('ROLE_MANAGER')")
    public List<ContentResponse> getAllContents() {
        List<Content> contents = contentRepository.findAll();
        return contentMapper.toContentResponseList(contents);
    }

    @Transactional(readOnly = true)
    @PostAuthorize( "hasAuthority('ROLE_MANAGER') or returnObject.getContentTargetType() == 'GLOBAL' " +
            "or @authorize.hasRole('MEMBER', returnObject.getContentTargetType(), returnObject.getTargetId())" )
    public ContentResponse getContentById(Long id) {
        Content content = entityLookupService.getContentById(id);
        return contentMapper.toContentResponse(content);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or @authorize.hasRole('MEMBER', #contentTargetType, #targetId)")
    public List<ContentResponse> getContentsByTargetTypeAndId(ContentTargetType contentTargetType, Long targetId) {
        List<Content> contents = contentRepository.findByContentTargetTypeAndTargetId(contentTargetType, targetId);
        return contentMapper.toContentResponseList(contents);
    }

    @Transactional(readOnly = true)
    public List<ContentResponse> getContentsByParent(Long parentId) {
        List<Content> contents = contentRepository.findByParentId(parentId);
        return contentMapper.toContentResponseList(contents);
    }
}