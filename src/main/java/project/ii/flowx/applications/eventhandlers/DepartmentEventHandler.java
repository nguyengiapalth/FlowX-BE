package project.ii.flowx.applications.eventhandlers;

import jakarta.persistence.EntityManager;
import org.springframework.context.event.EventListener;
import project.ii.flowx.applications.events.DepartmentEvent;
import project.ii.flowx.applications.service.auth.UserRoleService;
import project.ii.flowx.applications.service.helper.EntityLookupService;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;
import project.ii.flowx.model.dto.userrole.UserRoleCreateRequest;
import project.ii.flowx.model.entity.Role;
import project.ii.flowx.shared.enums.RoleScope;

public class DepartmentEventHandler {
    UserRoleService userRoleService;
    EntityLookupService entityLookupService;

    @EventListener
    public void handleManagerChangedEvent( DepartmentEvent.ManagerChangedEvent event) {
        Role managerRole = entityLookupService.getRoleByName("MANAGER")
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Manager role not found"));
        // Handle the manager change event
        long departmentId = event.departmentId();
        long oldManagerId = event.oldManagerId();

        if (oldManagerId != 0)
            userRoleService.
                    deleteUserRoleByUserIdAndRoleIdAndScope(oldManagerId, managerRole.getId(), RoleScope.DEPARTMENT, departmentId);

        long newManagerId = event.newManagerId();
        if(newManagerId != 0) {
            UserRoleCreateRequest userRoleCreateRequest = UserRoleCreateRequest.builder()
                    .userId(newManagerId)
                    .roleId(managerRole.getId())
                    .scope(RoleScope.DEPARTMENT)
                    .scopeId(departmentId)
                    .build();

            // Assign the manager role to the new manager
            userRoleService.assignRoleToUser(userRoleCreateRequest);

        }
    }
}
