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
import project.ii.flowx.applications.service.FileService;
import project.ii.flowx.applications.service.helper.EntityLookupService;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;
import project.ii.flowx.model.dto.content.*;
import project.ii.flowx.model.dto.file.FileResponse;
import project.ii.flowx.model.entity.Content;
import project.ii.flowx.model.entity.User;
import project.ii.flowx.model.mapper.ContentMapper;
import project.ii.flowx.model.repository.ContentRepository;
import project.ii.flowx.security.UserPrincipal;
import project.ii.flowx.shared.enums.ContentTargetType;
import project.ii.flowx.shared.enums.FileTargetType;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ContentService {
    ContentRepository contentRepository;
    ContentMapper contentMapper;
    EntityLookupService entityLookupService;
    FileService fileService;

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
        content.setHasFile(false); // Will be updated later when files are uploaded
        
        log.info("Creating content with title: {}", content.getTitle());
        content = contentRepository.save(content);
        
        ContentResponse response = contentMapper.toContentResponse(content);
        return populateFiles(response);
    }

    @Transactional
    @PreAuthorize("@authorize.isContentAuthor(id)")
    public ContentResponse updateContent(Long id, ContentUpdateRequest request) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Content not found"));
        
        contentMapper.updateContentFromRequest(content, request);
        // Note: hasFile will be managed separately when files are uploaded/removed
        
        content = contentRepository.save(content);
        ContentResponse response = contentMapper.toContentResponse(content);
        return populateFiles(response);
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
        List<ContentResponse> responses = contentMapper.toContentResponseList(contents);
        return populateFilesList(responses);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or @authorize.canAccessScope(#targetId, #contentTargetType)")
    public List<ContentResponse> getContentsByTargetTypeAndId(ContentTargetType contentTargetType, Long targetId) {
        List<Content> contents = contentRepository.findByContentTargetTypeAndTargetId(contentTargetType, targetId);
        List<ContentResponse> responses = contentMapper.toContentResponseList(contents);
        return populateFilesList(responses);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or @authorize.canAccessContent(#parentId)")
    public List<ContentResponse> getContentsByParent(Long parentId) {
        List<Content> contents = contentRepository.findByParentId(parentId);
        List<ContentResponse> responses = contentMapper.toContentResponseList(contents);
        return populateFilesList(responses);
    }

    @Transactional(readOnly = true)
    @PostAuthorize( "hasAuthority('ROLE_MANAGER') or @authorize.canAccessContent(#id)" )
    public ContentResponse getContentById(Long id) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Content not found"));
        ContentResponse response = contentMapper.toContentResponse(content);
        return populateFiles(response);
    }

    private Long getUserId() {
        var context = SecurityContextHolder.getContext();
        if (context.getAuthentication() == null || context.getAuthentication().getPrincipal() == null)
            throw new FlowXException(FlowXError.UNAUTHORIZED, "No authenticated user found");

        UserPrincipal userPrincipal = (UserPrincipal) context.getAuthentication().getPrincipal();
        return userPrincipal.getId();
    }

    private ContentResponse populateFiles(ContentResponse contentResponse) {
        if (contentResponse == null) return null;
        
        try {

            if (contentResponse.isHasFile()) {
                // Only call fileService if hasFile is true
                List<FileResponse> files = fileService.getFilesByEntity(FileTargetType.CONTENT, contentResponse.getId());
                contentResponse.setFiles(files);
                
                if (files.isEmpty()) {
                    // hasFile is true but no files found, update database
                    Content content = contentRepository.findById(contentResponse.getId()).orElse(null);
                    if (content != null) {
                        content.setHasFile(false);
                        contentRepository.save(content);
                    }
                    contentResponse.setHasFile(false);
                    log.info("Updated hasFile to false for content {} - no files found", contentResponse.getId());
                } else {
                    contentResponse.setHasFile(true);
                }
            } else {
                // hasFile is false or content not found, don't call fileService
                contentResponse.setFiles(List.of());
                contentResponse.setHasFile(false);
            }
            
            // Recursively populate files for replies
            if (contentResponse.getReplies() != null && !contentResponse.getReplies().isEmpty()) {
                List<ContentResponse> populatedReplies = contentResponse.getReplies().stream()
                        .map(this::populateFiles)
                        .toList();
                contentResponse.setReplies(populatedReplies);
            }
        } catch (Exception e) {
            log.warn("Failed to populate files for content {}: {}", contentResponse.getId(), e.getMessage());
            contentResponse.setFiles(List.of());
            contentResponse.setHasFile(false);
        }
        
        return contentResponse;
    }

    private List<ContentResponse> populateFilesList(List<ContentResponse> contentResponses) {
        return contentResponses.stream()
                .map(this::populateFiles)
                .toList();
    }

    @Transactional
    @PreAuthorize("@authorize.canAccessContent(#contentId)")
    public void updateHasFileFlag(Long contentId) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("Content not found"));
        
        try {
            List<FileResponse> files = fileService.getFilesByEntity(FileTargetType.CONTENT, contentId);
            boolean hasFiles = !files.isEmpty();
            
            if (content.isHasFile() != hasFiles) {
                content.setHasFile(hasFiles);
                contentRepository.save(content);
                log.info("Updated hasFile flag for content {} to {}", contentId, hasFiles);
            }
        } catch (Exception e) {
            log.warn("Failed to update hasFile flag for content {}: {}", contentId, e.getMessage());
        }
    }

}