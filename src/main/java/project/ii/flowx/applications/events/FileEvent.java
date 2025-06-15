package project.ii.flowx.applications.events;

public class FileEvent {
    
    public record FileUploadedEvent(Long fileId, String fileName, Long uploaderId, Long entityId, String entityType, String objectKey) {}
    
    public record FileDeletedEvent(Long fileId, String fileName, Long uploaderId, Long entityId, String entityType, String objectKey) {}
    
    public record FileAccessedEvent(Long fileId, String fileName, Long accessedBy) {}
}