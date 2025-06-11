package project.ii.flowx.applications.events;

public class DepartmentEvent {
    public record DepartmentCreatedEvent(long departmentId, String name, String description) {}

    public record DepartmentUpdatedEvent(long departmentId, String name, String description) {}

    public record DepartmentDeletedEvent(long departmentId) {}

    public record DepartmentUserAddedEvent(long departmentId, long userId) {}

    public record DepartmentUserRemovedEvent(long departmentId, long userId) {}

    public record ManagerChangedEvent(long departmentId, long oldManagerId, long newManagerId) {}
}
