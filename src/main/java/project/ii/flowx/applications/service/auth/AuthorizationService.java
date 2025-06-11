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
import project.ii.flowx.model.entity.Task;
import project.ii.flowx.security.UserPrincipal;
import project.ii.flowx.shared.enums.ContentTargetType;
import project.ii.flowx.shared.enums.RoleScope;

import java.util.List;

@Component("authorize")
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthorizationService {
    UserRoleService userRoleService;
    EntityLookupService entityLookupService;

    public List<UserRoleResponse> getUserRoles(Long userId) {
        return userRoleService.getNonGlobalRolesForUser(userId);
    }

    public boolean hasRole(String roleName, RoleScope roleScope, Long scopeId) {
        if(roleScope == RoleScope.GLOBAL) return true;
        Long userId = getUserId();
        List<UserRoleResponse> userRoles = getUserRoles(userId);

        boolean has = userRoles.stream()
                .anyMatch(r -> r.getRole().getName().equals(roleName)
                        && r.getScope() == roleScope
                        && r.getScopeId().equals(scopeId));
        return has;
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

    public boolean canAssignRole(RoleScope roleScope, Long scopeId) {
        if (roleScope == RoleScope.DEPARTMENT)
            return hasDepartmentRole("MANAGER", scopeId);
        if (roleScope == RoleScope.PROJECT)
            return hasProjectRole("MANAGER", scopeId);
        else return false;
    }

    public boolean canAccessScope(Long targetId, ContentTargetType targetType) {
        if (targetType == ContentTargetType.GLOBAL) return true;
        if (targetType == ContentTargetType.PRIVATE) {
            // For PRIVATE content, user can only access their own content
            Long userId = getUserId();
            return userId.equals(targetId);
        }
        if (targetType == ContentTargetType.DEPARTMENT)
            return hasDepartmentRole("MEMBER", targetId) || hasDepartmentRole("MANAGER", targetId);
        if (targetType == ContentTargetType.PROJECT)
            return hasProjectRole("MEMBER", targetId) || hasProjectRole("MANAGER", targetId);
        if (targetType == ContentTargetType.TASK)
            return isTaskAssignee(targetId) || isTaskManager(targetId);

        return false; // No access for other types
    }

    public boolean isTaskAssignee(Long taskId) {
        Long userId = getUserId();
        Task task = entityLookupService.getTaskById(taskId);
        if (task == null) throw new FlowXException(FlowXError.NOT_FOUND, "Task not found with ID: " + taskId);
        if (task.getAssignee() == null) return false; // Task has no assignee

        return task.getAssignee().getId().equals(userId);
    }

    public boolean isTaskAssigner(Long taskId) {
        Long userId = getUserId();
        Task task = entityLookupService.getTaskById(taskId);
        if (task == null) throw new FlowXException(FlowXError.NOT_FOUND, "Task not found with ID: " + taskId);
        if (task.getAssigner() == null) return false; // Task has no assigner

        return task.getAssigner().getId().equals(userId);
    }

    public boolean isTaskManager(Long taskId) {
        Long userId = getUserId();
        Task task = entityLookupService.getTaskById(taskId);
        if (task == null) throw new FlowXException(FlowXError.NOT_FOUND, "Task not found with ID: " + taskId);
        if (task.getTargetType() == ContentTargetType.DEPARTMENT)
            return hasDepartmentRole("MANAGER", task.getTargetId());
        if (task.getTargetType() == ContentTargetType.PROJECT)
            return hasProjectRole("MANAGER", task.getTargetId());
        else return false;
    }

    public boolean canCreateTask(Long targetId, ContentTargetType targetType) {
        if (targetType == ContentTargetType.GLOBAL) return false;
        if (targetType == ContentTargetType.DEPARTMENT)
            return hasDepartmentRole("MEMBER", targetId) || hasDepartmentRole("MANAGER", targetId);
        if (targetType == ContentTargetType.PROJECT)
            return hasProjectRole("MEMBER", targetId) || hasProjectRole("MANAGER", targetId);
        return false; // No access for other types
    }

    public boolean isContentAuthor(Long contentId) {
        Long userId = getUserId();
        var content = entityLookupService.getContentById(contentId);
        if (content == null) throw new FlowXException(FlowXError.NOT_FOUND, "Content not found with ID: " + contentId);

        return content.getAuthor().getId().equals(userId);
    }

    public boolean isContentManager(Long contentId) {
        var content = entityLookupService.getContentById(contentId);
        if (content == null) throw new FlowXException(FlowXError.NOT_FOUND, "Content not found with ID: " + contentId);

        if (content.getContentTargetType() == ContentTargetType.DEPARTMENT)
            return hasDepartmentRole("MANAGER", content.getTargetId());
        if (content.getContentTargetType() == ContentTargetType.PROJECT)
            return hasProjectRole("MANAGER", content.getTargetId());
        return false;
    }

    public boolean canAccessContent(Long contentId) {
        Long userId = getUserId();
        var content = entityLookupService.getContentById(contentId);
        if (content == null) throw new FlowXException(FlowXError.NOT_FOUND, "Content not found with ID: " + contentId);

        // Check if the user is the author
        if (content.getAuthor().getId().equals(userId)) return true;
        // Check if the user has a manager role in the target department or project
        if (content.getContentTargetType() == ContentTargetType.GLOBAL) return true;
        if (content.getContentTargetType() == ContentTargetType.DEPARTMENT)
            return hasDepartmentRole("MANAGER", content.getTargetId());
        if (content.getContentTargetType() == ContentTargetType.PROJECT)
            return hasProjectRole("MANAGER", content.getTargetId());

        return false; // No access if not author or manager
    }

    public Long getUserId() {
        var context = SecurityContextHolder.getContext();
        if (context.getAuthentication() == null || context.getAuthentication().getPrincipal() == null)
            throw new FlowXException(FlowXError.UNAUTHORIZED, "No authenticated user found");

        UserPrincipal userPrincipal = (UserPrincipal) context.getAuthentication().getPrincipal();
        return userPrincipal.getId();
    }
}