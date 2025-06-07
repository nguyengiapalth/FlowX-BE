package project.ii.flowx.applications.events;

public class FileEvent {
    
    public record FileUploadedEvent(Long fileId, String fileName, Long uploaderId, Long entityId, String entityType) {}
    
    public record FileDeletedEvent(Long fileId, String fileName, Long uploaderId) {}
    
    public record FileAccessedEvent(Long fileId, String fileName, Long accessedBy) {}
    
    public record FileSharedEvent(Long fileId, String fileName, Long sharedBy, String sharedWith) {}
} 