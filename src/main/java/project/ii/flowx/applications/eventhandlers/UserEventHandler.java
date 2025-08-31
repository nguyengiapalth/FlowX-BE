package project.ii.flowx.applications.eventhandlers;

import jakarta.mail.MessagingException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import project.ii.flowx.applications.events.UserEvent;
import project.ii.flowx.module.auth.service.RoleService;
import project.ii.flowx.module.auth.service.UserRoleService;
import project.ii.flowx.module.notify.NotificationService;
import project.ii.flowx.applications.helper.EntityLookupService;
import project.ii.flowx.applications.helper.MailService;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;
import project.ii.flowx.module.notify.dto.NotificationCreateRequest;
import project.ii.flowx.module.auth.dto.userrole.UserRoleCreateRequest;
import project.ii.flowx.module.auth.entity.Role;
import project.ii.flowx.applications.enums.RoleScope;

import java.io.IOException;

@Component
@EnableAsync
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserEventHandler {
    UserRoleService userRoleService;
    NotificationService notificationService;
    RoleService roleService;
    MailService mailService;

    @EventListener
    public void handleUserCreated(UserEvent.UserCreatedEvent event) throws MessagingException, IOException {
        log.info("User created: {}", event);

        Role role = roleService.getRoleByName("USER");

        UserRoleCreateRequest userRoleCreateRequest = UserRoleCreateRequest.builder()
                .userId(event.userId())
                .roleId(role.getId())
                .scope(RoleScope.GLOBAL)
                .scopeId(null) // Global scope, no specific scope ID needed
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

}
