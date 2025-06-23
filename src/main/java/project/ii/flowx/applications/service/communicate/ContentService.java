package project.ii.flowx.applications.service.communicate;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ii.flowx.applications.events.ContentEvent;
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
    ApplicationEventPublisher eventPublisher;

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_MANAGER') " +
            "or @authorize.canAccessScope(#request.targetId, #request.contentTargetType)")
//    @CacheEvict(value = "contents", key = "'allContents'", condition = "#result != null")
    public ContentResponse createContent(ContentCreateRequest request) {
         Long userId = getUserId();
         User author = entityLookupService.getUserById(userId);

        Content content = contentMapper.toContent(request);
        if (request.getParentId() != -1 && request.getParentId() != 0) {
            Content parent = contentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Parent content not found"));
            if(parent.getDepth() >= 2){
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
        
        // Publish content created event
        eventPublisher.publishEvent(new ContentEvent.ContentCreatedEvent(
                content.getId(),
                content.getBody(),
                content.getContentTargetType().toString(),
                userId
        ));
        
        // If this is a reply, publish reply event
        if (content.getParent() != null) {
            eventPublisher.publishEvent(new ContentEvent.ContentRepliedEvent(
                    content.getParent().getId(),
                    userId
            ));
        }
        
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
        
        // Publish content updated event
        eventPublisher.publishEvent(new ContentEvent.ContentUpdatedEvent(
                content.getId(),
                content.getBody(),
                content.getContentTargetType().toString()
        ));
        
        ContentResponse response = contentMapper.toContentResponse(content);
        return populateFiles(response);
    }

    @Transactional
//    @CacheEvict(value = "contents", key = "'allContents'", condition = "#result != null")
    public void updateHasFileFlag(Long contentId, Long fileId) {
        // Update hasFile flag based on actual file count
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Content not found"));

        try {
            List<FileResponse> files = fileService.getFilesByEntity(FileTargetType.CONTENT, contentId);
            boolean hasFiles = !files.isEmpty();

            if (content.isHasFile() != hasFiles) {
                content.setHasFile(hasFiles);
                contentRepository.save(content);
                log.info("Updated hasFile flag for content {}: {}", contentId, hasFiles);
            }
        }
        catch (Exception e) {
            log.error("Error updating hasFile flag for content {}: {}", contentId, e.getMessage());
        }

        // Check for avatar/background updates synchronously within same transaction
        try {
            if (content.getSubtitle() != null) {
                String subtitle = content.getSubtitle().toLowerCase();
                log.debug("Checking subtitle '{}' for avatar/background update patterns", subtitle);
                
                if (subtitle.contains("đã cập nhật ảnh đại diện") || subtitle.contains("updated avatar")) {
                    log.info("Avatar update detected for content {}: subtitle '{}'", contentId, content.getSubtitle());
                    // Trigger event synchronously within transaction
                    ContentEvent.AvatarUpdatedEvent avatarUpdatedEvent = new ContentEvent.AvatarUpdatedEvent(content, fileId);
                    eventPublisher.publishEvent(avatarUpdatedEvent);
                } else if (subtitle.contains("đã thay đổi ảnh bìa") || subtitle.contains("updated background")) {
                    log.info("Background update detected for content {}: subtitle '{}'", contentId, content.getSubtitle());
                    // Trigger event synchronously within transaction
                    ContentEvent.BackgroundUpdatedEvent backgroundUpdatedEvent = new ContentEvent.BackgroundUpdatedEvent(content, fileId);
                    eventPublisher.publishEvent(backgroundUpdatedEvent);
                }
            }
        } catch (Exception e) {
            log.error("Error checking for avatar/background update: {}", e.getMessage(), e);
        }
        
        log.debug("Completed hasFile flag and avatar/background update for content {}", contentId);
    }

    @Transactional
    @PreAuthorize("@authorize.isContentAuthor(#id) or hasAuthority('ROLE_MANAGER') or @authorize.isContentManager(#id)")
    public void deleteContent(Long id) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Content not found"));
        
        // Publish content deleted event
        eventPublisher.publishEvent(new ContentEvent.ContentDeletedEvent(id));
        
        contentRepository.delete(content);
    }

    @Transactional(readOnly = true)
    @PreAuthorize( "isAuthenticated()")
//    @Cacheable(value = "contents", key = "'allContents'", unless = "#result == null || #result.isEmpty()")
    public List<ContentResponse> getAllContents() {
        List<Content> contents = contentRepository.findByDepthOrderByCreatedAtDesc(0);
        log.info("Retrieved {} contents from database", contents);
        log.info("Content IDs: {}", contents.stream().map(Content::getId).collect(Collectors.toList()));

        return contentMapper.toContentResponseList(contents);
        // todo: pagination, graphql and personalized with microservice, but not for this app
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or @authorize.canAccessScope(#targetId, #contentTargetType)")
    public List<ContentResponse> getContentsByTargetTypeAndId(ContentTargetType contentTargetType, Long targetId) {
        List<Content> contents = contentRepository.findByContentTargetTypeAndTargetIdOrderByCreatedAtDesc(contentTargetType, targetId);
        return contentMapper.toContentResponseList(contents);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
//    @Cacheable(value = "contents", key = "'userContents-' + #userId", unless = "#result == null || #result.isEmpty()")
    public List<ContentResponse> getContentsByUser(Long userId) {
        User user = entityLookupService.getUserById(userId);
        List<Content> contents = contentRepository.findByAuthorOrderByCreatedAtDesc(user);
        return contentMapper.toContentResponseList(contents);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or @authorize.canAccessContent(#parentId)")
    public List<ContentResponse> getContentsByParent(Long parentId) {
        List<Content> contents = contentRepository.findByParentIdOrderByCreatedAtAsc(parentId);
        return contentMapper.toContentResponseList(contents);
    }

    @Transactional(readOnly = true)
    @PostAuthorize( "hasAuthority('ROLE_MANAGER') or @authorize.canAccessContent(#id)" )
    public ContentResponse getContentById(Long id) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Content not found"));
        
        // Publish content viewed event
        Long userId = getUserId();
        eventPublisher.publishEvent(new ContentEvent.ContentViewedEvent(id, userId));
        
        ContentResponse response = contentMapper.toContentResponse(content);
        return populateFiles(response);
    }

    // Helper method to get the current authenticated user's ID
    private Long getUserId() {
        var context = SecurityContextHolder.getContext();
        if (context.getAuthentication() == null || context.getAuthentication().getPrincipal() == null)
            throw new FlowXException(FlowXError.UNAUTHENTICATED, "No authenticated user found");

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

    public List<ContentResponse> filterAccessibleContents(List<ContentResponse> contents) {
        return contents.stream()
                .filter(content -> authorizationService.canAccessContent(content.getId()))
                .collect(Collectors.toList());
    }

    public List<ContentResponse> filterAndPopulateFiles(List<ContentResponse> contents) {
        List<ContentResponse> accessibleContents = filterAccessibleContents(contents);
        return populateFilesList(accessibleContents);
    }
}