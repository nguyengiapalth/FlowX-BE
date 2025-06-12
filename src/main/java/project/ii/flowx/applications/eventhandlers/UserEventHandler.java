package project.ii.flowx.applications.eventhandlers;

import jakarta.mail.MessagingException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import project.ii.flowx.applications.events.UserEvent;
import project.ii.flowx.applications.service.auth.UserRoleService;
import project.ii.flowx.applications.service.communicate.NotificationService;
import project.ii.flowx.applications.service.helper.EntityLookupService;
import project.ii.flowx.applications.service.helper.MailService;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;
import project.ii.flowx.model.dto.notification.NotificationCreateRequest;
import project.ii.flowx.model.dto.notification.NotificationResponse;
import project.ii.flowx.model.dto.userrole.UserRoleCreateRequest;
import project.ii.flowx.model.entity.Role;
import project.ii.flowx.shared.enums.RoleScope;

import java.io.IOException;

@Component
@EnableAsync
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserEventHandler {
    UserRoleService userRoleService;
    NotificationService notificationService;
    EntityLookupService entityLookupService;
    MailService mailService;

    @EventListener
    public void handleUserCreated(UserEvent.UserCreatedEvent event) throws MessagingException, IOException {
        log.info("User created: {}", event);

        Role role = entityLookupService.getRoleByName("USER")
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Role not found"));

        UserRoleCreateRequest userRoleCreateRequest = UserRoleCreateRequest.builder()
                .userId(event.userId())
                .roleId(role.getId())
                .scope(RoleScope.GLOBAL)
                .scopeId(0L)
                .build();


        log.info("Assigning role {} to user {}", role.getName(), event.userId());
        log.info("Role request: {}", userRoleCreateRequest);
        userRoleService.assignRoleToUser(userRoleCreateRequest);

        // Send welcome notification
        NotificationCreateRequest notificationCreateRequest = NotificationCreateRequest.builder()
                .userId(event.userId())
                .title("Welcome to FlowX")
                .content("Hello " + event.fullName() + ",\n\nWelcome to FlowX! We are glad to have you on board.\n\nBest regards,\nFlowX Team")
                .build();

        notificationService.createNotification(notificationCreateRequest);

        // Send welcome email
        mailService.sendWelcomeEmail(event);
    }

    @EventListener
    public void handleUserUpdated(UserEvent.UserUpdatedEvent event) {
        log.info("User updated: {}", event);
    }

    @EventListener
    public void handleUserDeleted(UserEvent.UserDeletedEvent event) {
        log.info("User deleted: {}", event);
    }

    @EventListener
    public void handleDepartmentChanged(UserEvent.UserDepartmentChangedEvent event) {
        log.info("User department changed: {}", event);
        // delete old department role
        if(event.oldDepartmentId() != 0)
            userRoleService.deleteUserRolesByUserIdAndScope(event.userId(), RoleScope.DEPARTMENT, event.oldDepartmentId());
        if (event.departmentId() == 0) return;

        // assign new department role
        var role = entityLookupService.getRoleByName("MEMBER")
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Role not found"));
        UserRoleCreateRequest userRoleCreateRequest = UserRoleCreateRequest.builder()
                .userId(event.userId())
                .roleId(role.getId())
                .scope(RoleScope.DEPARTMENT)
                .scopeId(event.departmentId())
                .build();
        userRoleService.assignRoleToUser(userRoleCreateRequest);

        log.info("Assigning role {} to user {}", role.getName(), event.userId());
        // send notification
        NotificationCreateRequest notificationCreateRequest = NotificationCreateRequest.builder()
                .userId(event.userId())
                .title("Department Changed")
                .content("Your department has been changed to " + event.departmentId() + ".")
                .build();
        notificationService.createNotification(notificationCreateRequest);
    }
}
