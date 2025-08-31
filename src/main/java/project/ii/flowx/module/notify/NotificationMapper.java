package project.ii.flowx.module.notify;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import project.ii.flowx.module.notify.dto.NotificationCreateRequest;
import project.ii.flowx.module.notify.dto.NotificationResponse;
import project.ii.flowx.module.notify.dto.NotificationTarget;

import java.util.List;

/**
 * Mapper interface for converting between Notification entity and Notification DTOs.
 * This interface uses MapStruct to generate the implementation at compile time.
 * Handles JSON serialization/deserialization for NotificationTarget objects.
 */
@Mapper(componentModel = "spring")
public interface NotificationMapper {

    /**
     * Convert Notification entity to NotificationResponse DTO
     * Deserializes JSON target field to NotificationTarget object
     */
    @Mapping(target = "target", source = "target", qualifiedByName = "jsonToNotificationTarget")
    NotificationResponse toNotificationResponse(Notification notification);

    /**
     * Convert NotificationCreateRequest to Notification entity
     * Serializes NotificationTarget object to JSON and ignores system-managed fields
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "readAt", ignore = true)
    @Mapping(target = "isRead", ignore = true) // Will be set to false by service
    @Mapping(target = "target", source = "target", qualifiedByName = "notificationTargetToJson")
    Notification toNotification(NotificationCreateRequest notificationCreateRequest);

    /**
     * Convert list of Notification entities to list of NotificationResponse DTOs
     */
    List<NotificationResponse> toNotificationResponseList(List<Notification> notifications);

    /**
     * Serialize NotificationTarget object to JSON string
     * Handles null values gracefully and provides error handling
     */
    @Named("notificationTargetToJson")
    default String notificationTargetToJson(NotificationTarget target) {
        if (target == null) {
            return null;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.findAndRegisterModules(); // Support for Java time types
            return mapper.writeValueAsString(target);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing NotificationTarget to JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Deserialize JSON string to NotificationTarget object
     * Handles null values gracefully and provides error handling
     */
    @Named("jsonToNotificationTarget")
    default NotificationTarget jsonToNotificationTarget(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.findAndRegisterModules(); // Support for Java time types
            return mapper.readValue(json, NotificationTarget.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error deserializing JSON to NotificationTarget: " + e.getMessage(), e);
        }
    }
}