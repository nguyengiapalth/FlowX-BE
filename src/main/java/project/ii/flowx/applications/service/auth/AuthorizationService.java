package project.ii.flowx.applications.service.auth;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import project.ii.flowx.applications.service.helper.EntityLookupService;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;
import project.ii.flowx.model.dto.userrole.UserRoleResponse;
import project.ii.flowx.model.entity.Project;
import project.ii.flowx.security.UserPrincipal;
import project.ii.flowx.shared.enums.RoleScope;

import java.util.List;

@Component("authorize")
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthorizationService {
    UserRoleService userRoleService;
    EntityLookupService entityLookupService;

    public List<UserRoleResponse> getUserAllRoles(Long userId) {
        return userRoleService.getNonGlobalRolesForUser(userId);
    }

    public boolean hasRole(String roleName, RoleScope roleScope, Long scopeId) {
        Long userId = getUserId();
        List<UserRoleResponse> userRoles = getUserAllRoles(userId);

        return userRoles.stream()
                .anyMatch(r -> r.getRole().getName().equals(roleName)
                        && r.getScope() == roleScope
                        && r.getScopeId().equals(scopeId));
    }

    public boolean hasProjectRole(String roleName, Long projectId) {
        if (roleName == null || projectId == null) return false;
        if (roleName.equalsIgnoreCase("MEMBER")) return hasRole(roleName, RoleScope.PROJECT, projectId);

        Project project = entityLookupService.getProjectById(projectId);
        return hasDepartmentRole(roleName, project.getDepartment().getId()) || hasRole(roleName, RoleScope.PROJECT, projectId);
    }

    public boolean hasDepartmentRole(String roleName, Long departmentId) {
        if (roleName == null || departmentId == null) return false;
        return hasRole(roleName, RoleScope.DEPARTMENT, departmentId);
    }

    public Long getUserId() {
        var context = SecurityContextHolder.getContext();
        if (context.getAuthentication() == null || context.getAuthentication().getPrincipal() == null)
            throw new FlowXException(FlowXError.UNAUTHORIZED, "No authenticated user found");

        UserPrincipal userPrincipal = (UserPrincipal) context.getAuthentication().getPrincipal();
        return userPrincipal.getId();
    }
}