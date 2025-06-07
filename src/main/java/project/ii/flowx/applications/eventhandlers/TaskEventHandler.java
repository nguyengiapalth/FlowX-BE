package project.ii.flowx.applications.eventhandlers;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import project.ii.flowx.applications.service.auth.UserRoleService;
import project.ii.flowx.applications.service.communicate.NotificationService;
import project.ii.flowx.applications.service.helper.EntityLookupService;

@Component
@EnableAsync
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TaskEventHandler {
    UserRoleService userRoleService;
    NotificationService notificationService;
    EntityLookupService entityLookupService;
}
