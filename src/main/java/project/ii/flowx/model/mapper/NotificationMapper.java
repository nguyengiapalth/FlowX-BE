package project.ii.flowx.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import project.ii.flowx.model.dto.notification.NotificationCreateRequest;
import project.ii.flowx.model.dto.notification.NotificationResponse;
import project.ii.flowx.model.dto.notification.NotificationUpdateRequest;
import project.ii.flowx.model.entity.Notification;

import java.util.List;

/**
 * Mapper interface for converting between Notification entity and Notification DTOs.
 * This interface uses MapStruct to generate the implementation at compile time.
 */
@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationResponse toNotificationResponse(Notification notification);

    @Mapping(target = "user.id", source = "userId")
    Notification toNotification(NotificationCreateRequest notificationCreateRequest);

    void updateNotificationFromRequest(@MappingTarget Notification notification, NotificationUpdateRequest notificationUpdateRequest);

    List<NotificationResponse> toNotificationResponseList(List<Notification> notifications);
}