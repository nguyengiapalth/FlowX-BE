package project.ii.flowx.applications.events;

import java.util.UUID;

public class TaskEvent {

    public record TaskCreatedEvent(UUID taskId, UUID userId, String taskTitle, String targetType){}

    public record TaskUpdatedEvent(UUID taskId, UUID userId, String taskTitle){}

    public record TaskDeletedEvent(UUID taskId, UUID userId, String taskTitle){}

    public record TaskAssignedEvent(UUID taskId, UUID assigneeId, UUID assignerId, String taskTitle){}

    public record TaskUnassignedEvent(UUID taskId, UUID previousAssigneeId, String taskTitle){}

    public record TaskCompletedEvent(UUID taskId, UUID userId, String taskTitle){}

    public record TaskStatusChangedEvent(UUID taskId, UUID userId, String oldStatus, String newStatus, String taskTitle){}

    public record TaskDueDateReminderEvent(UUID taskId, UUID assigneeId, String taskTitle, int daysUntilDue){}

    public record TaskOverdueEvent(UUID taskId, UUID assigneeId, String taskTitle, long daysOverdue){}
}
