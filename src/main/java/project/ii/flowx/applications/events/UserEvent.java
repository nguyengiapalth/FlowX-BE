package project.ii.flowx.applications.events;

import java.util.UUID;

public class UserEvent {

    public record UserCreatedEvent(UUID userId, String email, String password, String fullName, String position){}

    public record UserUpdatedEvent(UUID userId) {}

    public record UserDeletedEvent(UUID userId) {}


}
