package project.ii.flowx.applications.events;

public class TaskEvent {

    public record TaskCreatedEvent(long taskId, long userId, String taskTitle, String targetType){}

    public record TaskUpdatedEvent(long taskId, long userId, String taskTitle){}

    public record TaskDeletedEvent(long taskId, long userId, String taskTitle){}

    public record TaskAssignedEvent(long taskId, long assigneeId, long assignerId, String taskTitle){}

    public record TaskUnassignedEvent(long taskId, long previousAssigneeId, String taskTitle){}

    public record TaskCompletedEvent(long taskId, long userId, String taskTitle){}

    public record TaskStatusChangedEvent(long taskId, long userId, String oldStatus, String newStatus, String taskTitle){}

    public record TaskDueDateReminderEvent(long taskId, long assigneeId, String taskTitle, int daysUntilDue){}

    public record TaskOverdueEvent(long taskId, long assigneeId, String taskTitle, int daysOverdue){}
}
