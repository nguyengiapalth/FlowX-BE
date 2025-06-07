package project.ii.flowx.applications.eventhandlers;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import project.ii.flowx.applications.events.FileEvent;
import project.ii.flowx.applications.service.communicate.NotificationService;
import project.ii.flowx.applications.service.manage.UserActivityLogService;
import project.ii.flowx.model.dto.notification.NotificationCreateRequest;
import project.ii.flowx.model.dto.useractivitylog.UserActivityLogCreateRequest;
import project.ii.flowx.model.mapper.FileEventMapper;

@Component
@EnableAsync
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileEventHandler {
    
    NotificationService notificationService;
    UserActivityLogService userActivityLogService;
    FileEventMapper fileEventMapper;

//    @EventListener
//    @Async
//    public void handleFileUploaded(FileEvent.FileUploadedEvent event) {
//        log.info("File uploaded: {} by user: {}", event.fileName(), event.uploaderId());
//
//        // Log activity using mapper
//        UserActivityLogCreateRequest activityLog = fileEventMapper.toUploadActivityLog(event);
//        userActivityLogService.logActivity(activityLog);
//
//        log.info("File upload event processed for file: {}", event.fileName());
//    }
//
//    @EventListener
//    @Async
//    public void handleFileDeleted(FileEvent.FileDeletedEvent event) {
//        log.info("File deleted: {} by user: {}", event.fileName(), event.uploaderId());
//
//        // Log activity using mapper
//        UserActivityLogCreateRequest activityLog = fileEventMapper.toDeleteActivityLog(event);
//        userActivityLogService.logActivity(activityLog);
//
//        log.info("File delete event processed for file: {}", event.fileName());
//    }
//
//    @EventListener
//    @Async
//    public void handleFileAccessed(FileEvent.FileAccessedEvent event) {
//        log.info("File accessed: {} by user: {}", event.fileName(), event.accessedBy());
//
//        // Log activity using mapper
//        UserActivityLogCreateRequest activityLog = fileEventMapper.toAccessActivityLog(event);
//        userActivityLogService.logActivity(activityLog);
//
//        log.info("File access event processed for file: {}", event.fileName());
//    }
//
//    @EventListener
//    @Async
//    public void handleFileShared(FileEvent.FileSharedEvent event) {
//        log.info("File shared: {} by user: {} with: {}",
//                event.fileName(), event.sharedBy(), event.sharedWith());
//
//        // Log activity using mapper
//        UserActivityLogCreateRequest activityLog = fileEventMapper.toShareActivityLog(event);
//        userActivityLogService.logActivity(activityLog);
//
//        // Create notification for shared user if it's a user ID
//        createSharedFileNotification(event);
//
//        log.info("File share event processed for file: {}", event.fileName());
//    }
//
//    private void createSharedFileNotification(FileEvent.FileSharedEvent event) {
//        try {
//            Long sharedUserId = Long.parseLong(event.sharedWith());
//
//            // Use mapper to create notification
//            NotificationCreateRequest notification = fileEventMapper.toFileSharedNotification(
//                    sharedUserId,
//                    event.fileName(),
//                    event.fileId()
//            );
//
//            notificationService.createNotification(notification);
//        } catch (NumberFormatException e) {
//            // sharedWith is not a user ID, could be email or group
//            log.debug("Shared with is not a user ID: {}", event.sharedWith());
//        }
//    }
}