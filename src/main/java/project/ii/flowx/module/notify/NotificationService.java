package project.ii.flowx.module.notify;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;
import project.ii.flowx.dto.PageResponse;
import project.ii.flowx.module.notify.dto.NotificationCreateRequest;
import project.ii.flowx.module.notify.dto.NotificationResponse;
import project.ii.flowx.security.UserPrincipal;

import java.util.UUID;

/**
 * Service class for managing notifications.
 * Creates, marks as read/unread, and retrieves notifications for users.
**/
@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@EnableAsync
public class NotificationService {
     NotificationRepository notificationRepository;
     NotificationMapper notificationMapper;
     SimpMessagingTemplate messagingTemplate;

    @Transactional()
    public void createNotification(NotificationCreateRequest createRequest) {
        // Use mapper to convert DTO to entity
        Notification notification = notificationMapper.toNotification(createRequest);
        notification.setIsRead(false);
        
        Notification savedNotification = notificationRepository.save(notification);
        NotificationResponse response = notificationMapper.toNotificationResponse(savedNotification);

        // Publish the notification to the user's WebSocket channel
        String topic = "/topic/notifications/" + createRequest.getUserId();
        
        try {
            messagingTemplate.convertAndSend(topic, response);
            log.info("Notification sent to WebSocket topic: {}", topic);
        }
        catch (Exception e) {
            log.error("Failed to send notification to WebSocket: {}", e.getMessage(), e);
        }
    }

    @Transactional()
    @PreAuthorize("isAuthenticated()")
    public void markAsRead(UUID id) {
        notificationRepository.markAsReadById(id);
    }

    @Transactional()
    @PreAuthorize("isAuthenticated()")
    public void markAllAsRead() {
        UUID currentUserId = getUserId();
        notificationRepository.markAllAsReadByUserId(currentUserId);
    }

    @Transactional()
    @PreAuthorize("isAuthenticated()")
    public void markAsUnread(UUID id) {
        notificationRepository.markAsUnreadById(id);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public PageResponse<NotificationResponse> getMyNotifications(int page) {
        UUID currentUserId = getUserId();

        Pageable pageable = PageRequest.of(page, 20);
        Page<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUserId,pageable );
        Page<NotificationResponse> responsePage = notifications.map(notificationMapper::toNotificationResponse);

        return new PageResponse<>(responsePage);
    }

    private UUID getUserId() {
        var context = SecurityContextHolder.getContext();
        if (context.getAuthentication() == null || context.getAuthentication().getPrincipal() == null)
            throw new FlowXException(FlowXError.UNAUTHENTICATED, "No authenticated user found");

        UserPrincipal userPrincipal = (UserPrincipal) context.getAuthentication().getPrincipal();
        return userPrincipal.getId();
    }
}
