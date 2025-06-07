package project.ii.flowx.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import project.ii.flowx.applications.events.FileEvent;
import project.ii.flowx.model.dto.notification.NotificationCreateRequest;
import project.ii.flowx.model.dto.useractivitylog.UserActivityLogCreateRequest;

@Mapper(componentModel = "spring")
public interface FileEventMapper {

    @Mapping(target = "userId", source = "uploaderId")
    @Mapping(target = "action", constant = "FILE_UPLOAD")
    @Mapping(target = "entityType", source = "entityType")
    @Mapping(target = "entityId", source = "entityId")
    @Mapping(target = "details", expression = "java(\"Uploaded file: \" + event.fileName())")
    @Mapping(target = "ipAddress", ignore = true)
    @Mapping(target = "userAgent", ignore = true)
    UserActivityLogCreateRequest toUploadActivityLog(FileEvent.FileUploadedEvent event);

    @Mapping(target = "userId", source = "uploaderId")
    @Mapping(target = "action", constant = "FILE_DELETE")
    @Mapping(target = "entityType", constant = "FILE")
    @Mapping(target = "entityId", source = "fileId")
    @Mapping(target = "details", expression = "java(\"Deleted file: \" + event.fileName())")
    @Mapping(target = "ipAddress", ignore = true)
    @Mapping(target = "userAgent", ignore = true)
    UserActivityLogCreateRequest toDeleteActivityLog(FileEvent.FileDeletedEvent event);

    @Mapping(target = "userId", source = "accessedBy")
    @Mapping(target = "action", constant = "FILE_ACCESS")
    @Mapping(target = "entityType", constant = "FILE")
    @Mapping(target = "entityId", source = "fileId")
    @Mapping(target = "details", expression = "java(\"Accessed file: \" + event.fileName())")
    @Mapping(target = "ipAddress", ignore = true)
    @Mapping(target = "userAgent", ignore = true)
    UserActivityLogCreateRequest toAccessActivityLog(FileEvent.FileAccessedEvent event);

    @Mapping(target = "userId", source = "sharedBy")
    @Mapping(target = "action", constant = "FILE_SHARE")
    @Mapping(target = "entityType", constant = "FILE")
    @Mapping(target = "entityId", source = "fileId")
    @Mapping(target = "details", expression = "java(\"Shared file: \" + event.fileName() + \" with: \" + event.sharedWith())")
    @Mapping(target = "ipAddress", ignore = true)
    @Mapping(target = "userAgent", ignore = true)
    UserActivityLogCreateRequest toShareActivityLog(FileEvent.FileSharedEvent event);

    @Mapping(target = "title", constant = "File Shared")
    @Mapping(target = "content", expression = "java(\"A file has been shared with you: \" + fileName)")
    @Mapping(target = "entityType", constant = "FILE")
    @Mapping(target = "entityId", source = "fileId")
    NotificationCreateRequest toFileSharedNotification(Long userId, String fileName, Long fileId);
} 