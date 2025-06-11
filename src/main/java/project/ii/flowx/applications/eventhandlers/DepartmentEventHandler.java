package project.ii.flowx.applications.eventhandlers;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import project.ii.flowx.applications.events.DepartmentEvent;
import project.ii.flowx.applications.service.auth.UserRoleService;
import project.ii.flowx.applications.service.helper.EntityLookupService;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;
import project.ii.flowx.model.dto.userrole.UserRoleCreateRequest;
import project.ii.flowx.model.entity.Role;
import project.ii.flowx.shared.enums.RoleScope;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class DepartmentEventHandler {
    UserRoleService userRoleService;
    EntityLookupService entityLookupService;

    @EventListener
    public void handleManagerChangedEvent( DepartmentEvent.ManagerChangedEvent event) {
        log.info("Handling manager changed event: departmentId={}, oldManagerId={}, newManagerId={}", 
                 event.departmentId(), event.oldManagerId(), event.newManagerId());
        
        Role managerRole = entityLookupService.getRoleByName("MANAGER")
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Manager role not found"));
        
        // Handle the manager change event
        long departmentId = event.departmentId();
        long oldManagerId = event.oldManagerId();

        if (oldManagerId != 0) {
            log.info("Removing manager role from user: {}", oldManagerId);
            userRoleService.deleteUserRoleByUserIdAndRoleIdAndScope(oldManagerId, managerRole.getId(), RoleScope.DEPARTMENT, departmentId);
        }

        long newManagerId = event.newManagerId();
        if(newManagerId != 0) {
            log.info("Assigning manager role to user: {}", newManagerId);
            UserRoleCreateRequest userRoleCreateRequest = UserRoleCreateRequest.builder()
                    .userId(newManagerId)
                    .roleId(managerRole.getId())
                    .scope(RoleScope.DEPARTMENT)
                    .scopeId(departmentId)
                    .build();

            // Assign the manager role to the new manager
            userRoleService.assignRoleToUser(userRoleCreateRequest);
        } else {
            log.info("No new manager assigned (newManagerId = 0)");
        }
        
        log.info("Manager changed event processed successfully");
    }
}
