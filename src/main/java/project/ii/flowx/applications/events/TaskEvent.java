package project.ii.flowx.applications.events;

public class TaskEvent {

    public record TaskCreatedEvent(long taskId, long userId){}

    public record TaskUpdatedEvent(long taskId, long userId){}

    public record TaskDeletedEvent(long taskId, long userId){}

    public record TaskAssignedEvent(long taskId, long userId){}

    public record TaskUnassignedEvent(long taskId, long userId){}

    public record TaskCompletedEvent(long taskId, long userId){}


}
