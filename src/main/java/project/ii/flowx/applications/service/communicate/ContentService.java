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
import project.ii.flowx.applications.service.auth.AuthorizationService;
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
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ContentService {
    ContentRepository contentRepository;
    ContentMapper contentMapper;
    EntityLookupService entityLookupService;
    FileService fileService;
    AuthorizationService authorizationService;

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or @authorize.canAccessScope(#request.targetId, #request.contentTargetType)")
    public ContentResponse createContent(ContentCreateRequest request) {
         Long userId = getUserId();
         User author = entityLookupService.getUserById(userId);

        Content content = contentMapper.toContent(request);
        if (request.getParentId() != -1 && request.getParentId() != 0) {
            Content parent = contentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Parent content not found"));
            if(parent.getDepth() >= 3){
                content.setDepth(parent.getDepth());
                content.setParent(parent.getParent());
            }
            else {
                content.setDepth(parent.getDepth() + 1);
                content.setParent(parent);
            }
        } else {
            content.setDepth(0);
            content.setParent(null);
        }
        content.setAuthor(author);
        content.setHasFile(false); // Will be updated later when files are uploaded
        
        content = contentRepository.save(content);
        
        ContentResponse response = contentMapper.toContentResponse(content);
        return populateFiles(response);
    }

    @Transactional
    @PreAuthorize("@authorize.isContentAuthor(id)")
    public ContentResponse updateContent(Long id, ContentUpdateRequest request) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Content not found"));
        
        contentMapper.updateContentFromRequest(content, request);
        // Note: hasFile will be managed separately when files are uploaded/removed
        
        content = contentRepository.save(content);
        ContentResponse response = contentMapper.toContentResponse(content);
        return populateFiles(response);
    }

    @Transactional
    @PreAuthorize("@authorize.isContentAuthor(#contentId)")
    public void updateHasFileFlag(Long contentId) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Content not found"));

        try {
            List<FileResponse> files = fileService.getFilesByEntity(FileTargetType.CONTENT, contentId);
            boolean hasFiles = !files.isEmpty();

            if (content.isHasFile() != hasFiles) {
                content.setHasFile(hasFiles);
                contentRepository.save(content);
            }
        } catch (Exception ignored) {}
    }

    @Transactional
    @PreAuthorize("@authorize.isContentAuthor(#id) or hasAuthority('ROLE_MANAGER') or @authorize.isContentManager(#id)")
    public void deleteContent(Long id) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Content not found"));
        contentRepository.delete(content);
    }

    @Transactional(readOnly = true)
    @PreAuthorize( "isAuthenticated()")
    public List<ContentResponse> getAllContents() {
        List<Content> contents = contentRepository.findAll();
        List<ContentResponse> responses = contentMapper.toContentResponseList(contents);
        // Filter out contents that the user cannot access
        responses = filterAccessibleContents(responses);
        return populateFilesList(responses);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or @authorize.canAccessScope(#targetId, #contentTargetType)")
    public List<ContentResponse> getContentsByTargetTypeAndId(ContentTargetType contentTargetType, Long targetId) {
        List<Content> contents = contentRepository.findByContentTargetTypeAndTargetIdOrderByCreatedAtDesc(contentTargetType, targetId);
        List<ContentResponse> responses = contentMapper.toContentResponseList(contents);
        // Filter out contents that the user cannot access
        responses = filterAccessibleContents(responses);
        return populateFilesList(responses);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<ContentResponse> getContentsByUser(Long userId) {
        User user = entityLookupService.getUserById(userId);
        List<Content> contents = contentRepository.findByAuthorOrderByCreatedAtDesc(user);
        List<ContentResponse> responses = contentMapper.toContentResponseList(contents);
        // Filter out contents that the user cannot access
        responses = filterAccessibleContents(responses);
        return populateFilesList(responses);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or @authorize.canAccessContent(#parentId)")
    public List<ContentResponse> getContentsByParent(Long parentId) {
        List<Content> contents = contentRepository.findByParentIdOrderByCreatedAtAsc(parentId);
        List<ContentResponse> responses = contentMapper.toContentResponseList(contents);
        return populateFilesList(responses);
    }

    @Transactional(readOnly = true)
    @PostAuthorize( "hasAuthority('ROLE_MANAGER') or @authorize.canAccessContent(#id)" )
    public ContentResponse getContentById(Long id) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Content not found"));
        ContentResponse response = contentMapper.toContentResponse(content);
        return populateFiles(response);
    }

    // Helper method to get the current authenticated user's ID
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

    private List<ContentResponse> filterAccessibleContents(List<ContentResponse> contents) {
        return contents.stream()
                .filter(content -> authorizationService.canAccessContent(content.getId()))
                .collect(Collectors.toList());
    }
}