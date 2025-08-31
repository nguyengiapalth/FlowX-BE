package project.ii.flowx.applications.eventhandlers;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import project.ii.flowx.applications.events.ProjectEvent;
import project.ii.flowx.module.auth.service.RoleService;
import project.ii.flowx.module.auth.service.UserRoleService;
import project.ii.flowx.module.notify.NotificationService;
import project.ii.flowx.applications.helper.EntityLookupService;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;
import project.ii.flowx.module.auth.dto.userrole.UserRoleCreateRequest;
import project.ii.flowx.module.auth.entity.Role;
import project.ii.flowx.applications.enums.RoleDefault;
import project.ii.flowx.applications.enums.RoleScope;

@Component
@EnableAsync
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectEventHandler {
    UserRoleService userRoleService;
    NotificationService notificationService;
    RoleService roleService;

    @EventListener
    public void handleProjectCreatedEvent(ProjectEvent.ProjectCreatedEvent event) {
        log.info("Project created: {}", event);
        /// Create a start post for the project

    }

    @EventListener
    public void handleProjectUpdatedEvent(ProjectEvent.ProjectUpdatedEvent event) {
        log.info("Project updated with ID: {}", event.projectId());
        // Handle project update logic here
        // send notification to members
    }

    @EventListener
    public void handleProjectDeletedEvent(ProjectEvent.ProjectDeletedEvent event) {
        log.info("Project deleted with ID: {}", event.projectId());
        userRoleService.deleteUserRolesByScope(RoleScope.PROJECT ,event.projectId());
        /// send notification to members
    }

    @EventListener
    public void handleAddMemberEvent(ProjectEvent.AddMemberEvent event) {
        Role role = roleService.getRoleByName("MEMBER");

        UserRoleCreateRequest userRoleCreateRequest = UserRoleCreateRequest.builder()
                .userId(event.userId())
                .roleId(role.getId())
                .scope(RoleScope.PROJECT)
                .scopeId(event.projectId())
                .build();

        userRoleService.assignRoleToUser(userRoleCreateRequest);
        if (event.role().equals(RoleDefault.MANAGER)) {
            Role managerRole = roleService.getRoleByName("MANAGER");
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
        Role role = roleService.getRoleByName("MANAGER");


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
