package project.ii.flowx.applications.events;

import project.ii.flowx.applications.enums.RoleDefault;

import java.util.UUID;

public class ProjectEvent {
    public record ProjectCreatedEvent(UUID projectId) {}

    public record ProjectUpdatedEvent(UUID projectId) {}

    public record ProjectDeletedEvent(UUID projectId) {}

    public record AddMemberEvent(UUID projectId, UUID userId, RoleDefault role) {}

    public record RemoveMemberEvent(UUID projectId, UUID userId) {}

    public record UpdateMemberRoleEvent(UUID projectId, UUID userId, RoleDefault newRole) {}
}
