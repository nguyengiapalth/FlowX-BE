package project.ii.flowx.applications.eventhandlers;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import project.ii.flowx.applications.events.ContentEvent;
import project.ii.flowx.applications.service.communicate.NotificationService;
import project.ii.flowx.applications.service.helper.EntityLookupService;
import project.ii.flowx.applications.service.manage.UserService;
import project.ii.flowx.applications.service.manage.DepartmentService;
import project.ii.flowx.applications.service.manage.ProjectService;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;
import project.ii.flowx.model.dto.content.ContentResponse;
import project.ii.flowx.model.dto.file.FileResponse;
import project.ii.flowx.model.dto.notification.NotificationCreateRequest;
import project.ii.flowx.model.entity.Content;
import project.ii.flowx.model.entity.File;
import project.ii.flowx.model.entity.User;
import project.ii.flowx.model.repository.ContentRepository;
import project.ii.flowx.shared.enums.ContentTargetType;

import java.util.List;

@Component
@EnableAsync
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ContentEventHandler {
    NotificationService notificationService;
    EntityLookupService entityLookupService;
    ContentRepository contentRepository;
    UserService userService;
    DepartmentService departmentService;
    ProjectService projectService;

    @EventListener
    @Async
    public void handleContentCreatedEvent(ContentEvent.ContentCreatedEvent event) {
        log.info("Content created event: {}", event);
        
        try {
            Content content = contentRepository.findById(event.contentId())
                    .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Content not found"));
            
            User author = entityLookupService.getUserById(event.userId());
            
            // Determine notification recipients based on content target type
            List<User> recipients = getNotificationRecipients(content);
            
            // Send notifications to relevant users
            for (User recipient : recipients) {
                if (!recipient.getId().equals(event.userId())) { // Don't notify the author
                    NotificationCreateRequest notificationRequest = NotificationCreateRequest.builder()
                            .userId(recipient.getId())
                            .title(getContentCreatedTitle(content.getContentTargetType()))
                            .content(String.format("%s đã tạo một nội dung mới: \"%s\"", 
                                    author.getFullName(), 
                                    truncateContent(event.description())))
                            .entityType("CONTENT")
                            .entityId(event.contentId())
                            .build();
                    
                    notificationService.createNotification(notificationRequest);
                }
            }
        } catch (Exception e) {
            log.error("Error handling content created event: {}", e.getMessage(), e);
        }
    }

    @EventListener
    @Async
    public void handleContentUpdatedEvent(ContentEvent.ContentUpdatedEvent event) {
        log.info("Content updated event: {}", event);
        
        try {
            Content content = contentRepository.findById(event.contentId())
                    .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Content not found"));
            
            User author = content.getAuthor();
            
            // Determine notification recipients based on content target type
            List<User> recipients = getNotificationRecipients(content);
            
            // Send notifications to relevant users who might be interested in updates
            for (User recipient : recipients) {
                if (!recipient.getId().equals(author.getId())) { // Don't notify the author
                    NotificationCreateRequest notificationRequest = NotificationCreateRequest.builder()
                            .userId(recipient.getId())
                            .title("Nội dung đã được cập nhật")
                            .content(String.format("%s đã cập nhật nội dung: \"%s\"", 
                                    author.getFullName(), 
                                    truncateContent(event.description())))
                            .entityType("CONTENT")
                            .entityId(event.contentId())
                            .build();
                    
                    notificationService.createNotification(notificationRequest);
                }
            }
        } catch (Exception e) {
            log.error("Error handling content updated event: {}", e.getMessage(), e);
        }
    }

    @EventListener
    @Async
    public void handleContentRepliedEvent(ContentEvent.ContentRepliedEvent event) {
        log.info("Content replied event: {}", event);
        
        try {
            Content parentContent = contentRepository.findById(event.contentId())
                    .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Parent content not found"));
            
            User replier = entityLookupService.getUserById(event.userId());
            User parentAuthor = parentContent.getAuthor();
            
            // Notify the parent content author
            if (!parentAuthor.getId().equals(event.userId())) {
                NotificationCreateRequest notificationRequest = NotificationCreateRequest.builder()
                        .userId(parentAuthor.getId())
                        .title("Có phản hồi mới")
                        .content(String.format("%s đã phản hồi nội dung của bạn: \"%s\"", 
                                replier.getFullName(), 
                                truncateContent(parentContent.getBody())))
                        .entityType("CONTENT")
                        .entityId(event.contentId())
                        .build();
                
                notificationService.createNotification(notificationRequest);
            }
            
            // Also notify other users who have replied to this content
            List<Content> otherReplies = contentRepository.findByParentIdOrderByCreatedAtAsc(event.contentId());
            for (Content reply : otherReplies) {
                User otherReplier = reply.getAuthor();
                if (!otherReplier.getId().equals(event.userId()) && 
                    !otherReplier.getId().equals(parentAuthor.getId())) {
                    
                    NotificationCreateRequest notificationRequest = NotificationCreateRequest.builder()
                            .userId(otherReplier.getId())
                            .title("Có phản hồi mới trong cuộc thảo luận")
                            .content(String.format("%s đã tham gia cuộc thảo luận: \"%s\"", 
                                    replier.getFullName(), 
                                    truncateContent(parentContent.getBody())))
                            .entityType("CONTENT")
                            .entityId(event.contentId())
                            .build();
                    
                    notificationService.createNotification(notificationRequest);
                }
            }
        } catch (Exception e) {
            log.error("Error handling content replied event: {}", e.getMessage(), e);
        }
    }

    @EventListener
    @Async
    public void handleContentDeletedEvent(ContentEvent.ContentDeletedEvent event) {
        log.info("Content deleted event: {}", event);
        // This event is mainly for cleanup purposes
        // Could potentially notify moderators or admins if needed
    }

    @EventListener
    public void handleAvatarUpdatedEvent(ContentEvent.AvatarUpdatedEvent event) {
        log.info("Avatar updated event: {}", event);
        try {
            Content content = event.content();
            File fileResponse = entityLookupService.getFileById(event.fileId());
            userService.updateUserAvatarObjectKey(content.getAuthor().getId(), fileResponse.getObjectKey());
            log.info("Avatar updated synchronously for user: {}", content.getAuthor().getId());
        } catch (Exception e) {
            log.error("Error updating avatar synchronously: {}", e.getMessage(), e);
        }
    }

    @EventListener
    public void handleBackgroundUpdatedEvent(ContentEvent.BackgroundUpdatedEvent event) {
        log.info("Background updated event: {}", event);
        try {
            Content content = event.content();
            File fileResponse = entityLookupService.getFileById(event.fileId());

            switch (content.getContentTargetType()) {
                case DEPARTMENT -> {
                    departmentService.updateDepartmentBackgroundObjectKey(
                            content.getTargetId(), fileResponse.getObjectKey());
                    log.info("Department background updated synchronously: {}", content.getTargetId());
                }
                case PROJECT -> {
                    projectService.updateProjectBackgroundObjectKey(
                            content.getTargetId(), fileResponse.getObjectKey());
                    log.info("Project background updated synchronously: {}", content.getTargetId());
                }
                case GLOBAL -> {
                    userService.updateUserBackgroundObjectKey(content.getAuthor().getId(), fileResponse.getObjectKey());
                    log.info("User background updated synchronously: {}", content.getAuthor().getId());
                }
                default -> log.warn("Unsupported content target type for background update: {}",
                        content.getContentTargetType());
            }
        } catch (Exception e) {
            log.error("Error updating background synchronously: {}", e.getMessage(), e);
        }
    }

    @EventListener
    @Async
    public void handleContentViewedEvent(ContentEvent.ContentViewedEvent event) {
        log.info("Content viewed event: {}", event);
        // This could be used for analytics or read receipts
        // For now, we'll just log it
    }

    private List<User> getNotificationRecipients(Content content) {
        // This method determines who should receive notifications based on content type
        // Implementation depends on your business logic
        
        if (content.getContentTargetType() == ContentTargetType.DEPARTMENT) {
            // Get users in the same department
            return entityLookupService.getUsersByDepartmentId(content.getTargetId());
        } else if (content.getContentTargetType() == ContentTargetType.PROJECT) {
            // Get users in the same project
            return entityLookupService.getUsersByProjectId(content.getTargetId());
        } else {
            // For GLOBAL content, you might want to limit notifications
            // or have a different strategy
            return List.of(); // Return empty list for now
        }
    }
    
    private String getContentCreatedTitle(ContentTargetType targetType) {
        return switch (targetType) {
            case DEPARTMENT -> "Nội dung mới trong phòng ban";
            case PROJECT -> "Nội dung mới trong dự án";
            case GLOBAL -> "Nội dung mới";
            default -> "Nội dung mới";
        };
    }
    
    private String truncateContent(String content) {
        if (content == null) return "";
        return content.length() > 100 ? content.substring(0, 100) + "..." : content;
    }


}
