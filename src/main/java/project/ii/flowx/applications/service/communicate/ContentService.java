package project.ii.flowx.applications.service.communicate;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ii.flowx.applications.service.helper.EntityLookupService;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;
import project.ii.flowx.model.dto.content.*;
import project.ii.flowx.model.entity.Content;
import project.ii.flowx.model.entity.User;
import project.ii.flowx.model.mapper.ContentMapper;
import project.ii.flowx.model.repository.ContentRepository;
import project.ii.flowx.security.UserPrincipal;
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
    @PreAuthorize( "hasAuthority('ROLE_MANAGER') " +
            "or @authorize.canAccessScope(#request.targetId, #request.contentTargetType)" )
    public ContentResponse createContent(ContentCreateRequest request) {
         Long userId = getUserId();
         User author = entityLookupService.getUserById(userId);

        Content content = contentMapper.toContent(request);
        if (request.getParentId() != -1) {
            Content parent = contentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("Parent content not found"));
            if(parent.getDepth() >= 3){
                content.setDepth(parent.getDepth());
                content.setParent(parent.getParent());
            }
            else content.setDepth(parent.getDepth() + 1);
        } else {
            content.setDepth(0);
        }
        content.setAuthor(author);
        log.info("Creating content with title: {}", content.getTitle());
        content = contentRepository.save(content);
        return contentMapper.toContentResponse(content);
    }

    @Transactional
    @PreAuthorize("@authorize.isContentAuthor(id)")
    public ContentResponse updateContent(Long id, ContentUpdateRequest request) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Content not found"));
        contentMapper.updateContentFromRequest(content, request);
        content = contentRepository.save(content);
        return contentMapper.toContentResponse(content);
    }

    @Transactional
    @PreAuthorize("@authorize.isContentAuthor(#id) " +
            "or hasAuthority('ROLE_MANAGER') or @authorize.isContentManager(#id)")
    public void deleteContent(Long id) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Content not found"));
        contentRepository.delete(content);
    }

    @Transactional(readOnly = true)
    @PreAuthorize( "hasAuthority('ROLE_MANAGER')")
    public List<ContentResponse> getAllContents() {
        List<Content> contents = contentRepository.findAll();
        return contentMapper.toContentResponseList(contents);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or @authorize.canAccessScope(#targetId, #contentTargetType)")
    public List<ContentResponse> getContentsByTargetTypeAndId(ContentTargetType contentTargetType, Long targetId) {
        List<Content> contents = contentRepository.findByContentTargetTypeAndTargetId(contentTargetType, targetId);
        return contentMapper.toContentResponseList(contents);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or @authorize.canAccessContent(#parentId)")
    public List<ContentResponse> getContentsByParent(Long parentId) {
        List<Content> contents = contentRepository.findByParentId(parentId);
        return contentMapper.toContentResponseList(contents);
    }

    @Transactional(readOnly = true)
    @PostAuthorize( "hasAuthority('ROLE_MANAGER') or @authorize.canAccessContent(#id)" )
    public ContentResponse getContentById(Long id) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Content not found"));
        return contentMapper.toContentResponse(content);
    }

    public Long getUserId() {
        var context = SecurityContextHolder.getContext();
        if (context.getAuthentication() == null || context.getAuthentication().getPrincipal() == null)
            throw new FlowXException(FlowXError.UNAUTHORIZED, "No authenticated user found");

        UserPrincipal userPrincipal = (UserPrincipal) context.getAuthentication().getPrincipal();
        return userPrincipal.getId();
    }

}