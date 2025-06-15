package project.ii.flowx.applications.events;


import project.ii.flowx.model.entity.Content;
import project.ii.flowx.shared.enums.ContentTargetType;

public class  ContentEvent {
    public record ContentCreatedEvent(long contentId, String description, String contentType, long userId) {}

    public record ContentUpdatedEvent(long contentId, String description, String contentType) {}

    public record ContentDeletedEvent(long contentId) {}

    public record ContentViewedEvent(long contentId, long userId) {}

    public record ContentSharedEvent(long contentId, long userId, String sharedWith) {}

    public record ContentRepliedEvent(long contentId, long userId) {}
    
    // Avatar and Background update events
    public record AvatarUpdatedEvent(Content content, Long fileId) {}
    
    public record BackgroundUpdatedEvent(Content content, Long fileId) {}
}
