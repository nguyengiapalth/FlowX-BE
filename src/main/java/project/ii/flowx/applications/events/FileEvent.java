package project.ii.flowx.applications.events;

import project.ii.flowx.applications.enums.FileTargetType;

import java.util.UUID;

public class FileEvent {
    
    public record FileUploadedEvent(UUID fileId, String fileName, UUID uploaderId, UUID targetId, FileTargetType targetType, String objectKey) {}
    
    public record FileDeletedEvent(UUID fileId, String fileName, UUID uploaderId, UUID targetId, FileTargetType targetType, String objectKey) {}
    
    public record FileAccessedEvent(UUID fileId, String fileName, UUID accessedBy) {}
}