package project.ii.flowx.applications.events;

import project.ii.flowx.shared.enums.MemberStatus;
import project.ii.flowx.shared.enums.RoleDefault;

public class ProjectEvent {
    public record ProjectCreatedEvent(long projectId, long departmentId) {}

    public record ProjectUpdatedEvent(long projectId) {}

    public record ProjectDeletedEvent(long projectId) {}

    public record AddMemberEvent(long projectId, long userId, RoleDefault role) {}

    public record RemoveMemberEvent(long projectId, long userId) {}

    public record UpdateMemberRoleEvent(long projectId, long userId, RoleDefault newRole) {}

    public record UpdateMemberStatusEvent(long projectId, long userId, RoleDefault newRole) {}
}
