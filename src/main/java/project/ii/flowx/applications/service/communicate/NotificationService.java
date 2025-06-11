package project.ii.flowx.applications.service.communicate;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;
import project.ii.flowx.model.entity.Notification;
import project.ii.flowx.model.dto.notification.NotificationCreateRequest;
import project.ii.flowx.model.dto.notification.NotificationResponse;
import project.ii.flowx.model.repository.NotificationRepository;
import project.ii.flowx.model.mapper.NotificationMapper;
import project.ii.flowx.applications.service.helper.EntityLookupService;
import project.ii.flowx.security.UserPrincipal;

import java.time.LocalDateTime;
import java.util.List;
/**
 * Service class for managing notifications.
 * Creates, marks as read/unread, and retrieves notifications for users.
**/
@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationService {
     NotificationRepository notificationRepository;
     NotificationMapper notificationMapper;
     SimpMessagingTemplate messagingTemplate;
     EntityLookupService entityLookupService;

    @Transactional()
    public NotificationResponse createNotification(NotificationCreateRequest createRequest) {
        // Create notification entity
        Notification notification = new Notification();
        notification.setTitle(createRequest.getTitle());
        notification.setContent(createRequest.getContent());
        notification.setEntityType(createRequest.getEntityType());
        notification.setEntityId(createRequest.getEntityId());
        notification.setIsRead(false);
        
        // Set user by ID
        notification.setUser(entityLookupService.getUserById(createRequest.getUserId()));
        
        Notification savedNotification = notificationRepository.save(notification);

        // Create response DTO
        NotificationResponse response = notificationMapper.toNotificationResponse(savedNotification);

        // Publish the notification to the user's WebSocket channel
        String topic = "/topic/notifications/" + createRequest.getUserId();
        
        try {
            messagingTemplate.convertAndSend(topic, response);
            log.info("Notification sent to WebSocket topic: {}", topic);
        } catch (Exception e) {
            log.error("Failed to send notification to WebSocket: {}", e.getMessage(), e);
        }

        return response;
    }

    @Transactional()
    @PreAuthorize("isAuthenticated()")
    public void markAsRead(Long id) {
        notificationRepository.markAsReadById(id);
    }

    @Transactional()
    @PreAuthorize("isAuthenticated()")
    public void markAllAsRead() {
        Long currentUserId = getUserId();
        notificationRepository.markAllAsReadByUserId(currentUserId);
    }

    @Transactional()
    @PreAuthorize("isAuthenticated()")
    public void markAsUnread(Long id) {
        notificationRepository.markAsUnreadById(id);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public Page<NotificationResponse> getMyNotifications(int page) {
        Long currentUserId = getUserId();

        Pageable pageable = PageRequest.of(page, 10);
        Page<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUserId,pageable );
        return notifications.map(notificationMapper::toNotificationResponse);
    }

    private Long getUserId() {
        var context = SecurityContextHolder.getContext();
        if (context.getAuthentication() == null || context.getAuthentication().getPrincipal() == null)
            throw new FlowXException(FlowXError.UNAUTHORIZED, "No authenticated user found");

        UserPrincipal userPrincipal = (UserPrincipal) context.getAuthentication().getPrincipal();
        return userPrincipal.getId();
    }
}
