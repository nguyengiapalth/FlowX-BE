package project.ii.flowx.applications.events;



public class ContentEvent {
    public record ContentCreatedEvent(long contentId, String description, String contentType, long userId) {}

    public record ContentUpdatedEvent(long contentId, String description, String contentType) {}

    public record ContentDeletedEvent(long contentId) {}

    public record ContentViewedEvent(long contentId, long userId) {}

    public record ContentSharedEvent(long contentId, long userId, String sharedWith) {}

    public record ContentRepliedEvent(long contentId, long userId) {}
}
