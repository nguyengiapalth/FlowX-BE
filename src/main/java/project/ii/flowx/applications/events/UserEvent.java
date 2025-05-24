package project.ii.flowx.applications.events;

public class UserEvent {

    public record UserCreatedEvent(long userId, String email, String password, String fullName, String position){}

    public record UserUpdatedEvent(long userId) {}

    public record UserDeletedEvent(long userId) {}

    public record UserDepartmentChangedEvent(long userId, long oldDepartmentId, long departmentId) {}
}
