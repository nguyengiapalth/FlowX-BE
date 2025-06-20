package project.ii.flowx.applications.eventhandlers;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import project.ii.flowx.applications.events.ProjectEvent;
import project.ii.flowx.applications.service.auth.UserRoleService;
import project.ii.flowx.applications.service.communicate.NotificationService;
import project.ii.flowx.applications.service.helper.EntityLookupService;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;
import project.ii.flowx.model.dto.userrole.UserRoleCreateRequest;
import project.ii.flowx.model.entity.Role;
import project.ii.flowx.shared.enums.RoleDefault;
import project.ii.flowx.shared.enums.RoleScope;

@Component
@EnableAsync
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectEventHandler {
    UserRoleService userRoleService;
    NotificationService notificationService;
    EntityLookupService entityLookupService;

    @EventListener
    public void handleProjectCreatedEvent(ProjectEvent.ProjectCreatedEvent event) {
        log.info("Project created: {}", event);
        // send socket event to user
        // NotificationCreateRequest notificationCreateRequest = ...
        // notificationService.createNotification(notificationCreateRequest);

    }

    @EventListener
    public void handleProjectUpdatedEvent(ProjectEvent.ProjectUpdatedEvent event) {
        log.info("Project updated with ID: {}", event.projectId());
        // Handle project update logic here
    }

    @EventListener
    public void handleProjectDeletedEvent(ProjectEvent.ProjectDeletedEvent event) {
        log.info("Project deleted with ID: {}", event.projectId());
        userRoleService.deleteUserRolesByScope(RoleScope.PROJECT ,event.projectId());
    }

    @EventListener
    public void handleAddMemberEvent(ProjectEvent.AddMemberEvent event) {
        Role role = entityLookupService.getRoleByName("MEMBER")
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Role not found"));

        UserRoleCreateRequest userRoleCreateRequest = UserRoleCreateRequest.builder()
                .userId(event.userId())
                .roleId(role.getId())
                .scope(RoleScope.PROJECT)
                .scopeId(event.projectId())
                .build();

        userRoleService.assignRoleToUser(userRoleCreateRequest);
        if (event.role().equals(RoleDefault.MANAGER)) {
            Role managerRole = entityLookupService.getRoleByName("MANAGER")
                    .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Manager role not found"));

            UserRoleCreateRequest managerRoleRequest = UserRoleCreateRequest.builder()
                    .userId(event.userId())
                    .roleId(managerRole.getId())
                    .scope(RoleScope.PROJECT)
                    .scopeId(event.projectId())
                    .build();
            userRoleService.assignRoleToUser(managerRoleRequest);
        }
        // send socket event to user
    }

    @EventListener
    public void handleRemoveMemberEvent(ProjectEvent.RemoveMemberEvent event) {
        log.info("Removing member {} from project {}", event.userId(), event.projectId());
        userRoleService.deleteUserRolesByUserIdAndScope(event.userId(), RoleScope.PROJECT, event.projectId());
    }

    @EventListener
    public void handleUpdateMemberEvent(ProjectEvent.UpdateMemberRoleEvent event) {
        Role role = entityLookupService.getRoleByName("MANAGER")
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Role not found"));

        if (event.newRole() == RoleDefault.MANAGER) {
            UserRoleCreateRequest userRoleCreateRequest = UserRoleCreateRequest.builder()
                    .userId(event.userId())
                    .roleId(role.getId())
                    .scope(RoleScope.PROJECT)
                    .scopeId(event.projectId())
                    .build();
            userRoleService.assignRoleToUser(userRoleCreateRequest);
        }
        else {
            // delete Manager role if exists
            userRoleService.deleteUserRoleByUserIdAndRoleIdAndScope(event.userId(), role.getId(), RoleScope.PROJECT, event.projectId());
        }
    }
}
