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
import project.ii.flowx.model.entity.Notification;
import project.ii.flowx.model.dto.notification.NotificationCreateRequest;
import project.ii.flowx.model.dto.notification.NotificationResponse;
import project.ii.flowx.model.repository.NotificationRepository;
import project.ii.flowx.model.mapper.NotificationMapper;
import project.ii.flowx.applications.service.helper.EntityLookupService;
import project.ii.flowx.security.UserPrincipal;

import java.time.Instant;
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
     EntityLookupService entityLookupService;

    SimpMessagingTemplate messagingTemplate;

    @Transactional()
    @PreAuthorize("isAuthenticated()")
    public NotificationResponse createNotification(NotificationCreateRequest createRequest) {
        log.info("Creating notification for user: {}", createRequest.getUserId());

        Notification notification = notificationMapper.toNotification(createRequest);
        notification.setIsRead(false);
        Notification savedNotification = notificationRepository.save(notification);

        log.info("Successfully created notification with ID: {}", savedNotification.getId());
        // Publish the notification to the user's WebSocket channel
        messagingTemplate.convertAndSend("/topic/notifications/" + createRequest.getUserId(),
            notificationMapper.toNotificationResponse(savedNotification));

        return notificationMapper.toNotificationResponse(savedNotification);
    }

    @Transactional()
    @PreAuthorize("isAuthenticated()")
    public void markAsRead(Long id) {
        log.info("Marking notification as read - ID: {}", id);

        Notification notification = entityLookupService.getNotificationById(id);
        notification.setIsRead(true);
        notification.setReadAt(Instant.now());
        notificationRepository.save(notification);

        log.info("Successfully marked notification {} as read", id);
    }

    @Transactional()
    @PreAuthorize("isAuthenticated()")
    public void markAllAsRead() {
        var context = SecurityContextHolder.getContext();
        var authentication = context.getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long currentUserId = userPrincipal.getId();

        log.info("Marking all notifications as read for user: {}", currentUserId);

        List<Notification> notifications = notificationRepository.findByUserId(currentUserId);
        for (Notification notification : notifications) {
            if (notification.getIsRead() == true) continue;
            notification.setIsRead(true);
            notification.setReadAt(Instant.now());
        }

        notificationRepository.saveAll(notifications);
        log.info("Successfully marked all notifications as read for user: {}", currentUserId);
    }

    @Transactional()
    @PreAuthorize("isAuthenticated()")
    public void markAsUnread(Long id) {
        log.info("Marking notification as unread - ID: {}", id);

        Notification notification = entityLookupService.getNotificationById(id);
        notification.setIsRead(false);
        notification.setReadAt(null);
        notificationRepository.save(notification);

        log.info("Successfully marked notification {} as unread", id);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public Page<NotificationResponse> getMyNotifications(int page) {
        var context = SecurityContextHolder.getContext();
        var authentication = context.getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long currentUserId = userPrincipal.getId();

        log.info("Fetching notifications for user: {}", currentUserId);

        Pageable pageable = PageRequest.of(page, 10);

        Page<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUserId,pageable );

        Page<NotificationResponse> responses = notifications.map(notificationMapper::toNotificationResponse);
        log.info("Successfully fetched {} notifications for user: {}", 10, currentUserId);
        return responses;
    }
}
