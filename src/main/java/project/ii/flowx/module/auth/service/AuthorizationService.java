package project.ii.flowx.module.auth.service;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import project.ii.flowx.applications.helper.EntityLookupService;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;
import project.ii.flowx.module.auth.dto.userrole.UserRoleResponse;
import project.ii.flowx.module.manage.entity.Task;
import project.ii.flowx.module.manage.service.TaskService;
import project.ii.flowx.security.UserPrincipal;
import project.ii.flowx.applications.enums.Visibility;
import project.ii.flowx.applications.enums.RoleScope;

import java.util.List;
import java.util.UUID;

@Component("authorize")
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthorizationService {
    UserRoleService userRoleService;
    TaskService taskService;
    EntityLookupService entityLookupService;

    public List<UserRoleResponse> getUserRoles(UUID userId) {
        log.info("Get roles for user {} in authorization service", userId);
        return userRoleService.getRolesForUser(userId);
    }

    public boolean hasRole(String roleName, RoleScope roleScope, UUID scopeId) {
        UUID userId = getUserId();
        List<UserRoleResponse> userRoles = getUserRoles(userId);

        return userRoles.stream()
                .anyMatch(r -> r.getRole().getName().equals(roleName)
                        && r.getScope() == roleScope
                        && (roleScope == RoleScope.GLOBAL ? scopeId == null || r.getScopeId() == null : r.getScopeId().equals(scopeId)));
    }

    public boolean isGlobalManager() {
        UUID userId = getUserId();
        List<UserRoleResponse> userRoles = getUserRoles(userId);
        return userRoles.stream()
                .anyMatch(r -> r.getRole().getName().equalsIgnoreCase("MANAGER")
                        && r.getScope() == RoleScope.GLOBAL);
    }

    public boolean hasProjectRole(String roleName, UUID projectId) {
        if (roleName == null || projectId == null) return false;
        return hasRole(roleName, RoleScope.PROJECT, projectId);
    }

    public boolean canAssignRole(RoleScope roleScope, UUID scopeId) {
        return hasProjectRole("MANAGER", scopeId);
    }

    public boolean canAccessScope(UUID targetId, Visibility targetType) {
        if (targetType == Visibility.GLOBAL) return true;
        if (targetType == Visibility.PRIVATE) {
            // For PRIVATE content, user can only access their own content
            UUID userId = getUserId();
            return userId.equals(targetId);
        }
        if (targetType == Visibility.PROJECT)
            return hasProjectRole("MEMBER", targetId) || hasProjectRole("MANAGER", targetId);

        return false;
    }

    /**
     * Check if current user is the assignee of the task
     */
    public boolean isTaskAssignee(UUID taskId) {
        UUID userId = getUserId();
        Task task = entityLookupService.getTaskById(taskId);
        if (task == null) {
            throw new FlowXException(FlowXError.NOT_FOUND, "Task not found with ID: " + taskId);
        }
        
        // Use assigneeId directly instead of assignee.getId()
        return task.getAssigneeId() != null && task.getAssigneeId().equals(userId);
    }

    /**
     * Check if current user is the assigner of the task
     */
    public boolean isTaskAssigner(UUID taskId) {
        UUID userId = getUserId();
        Task task = entityLookupService.getTaskById(taskId);
        if (task == null) {
            throw new FlowXException(FlowXError.NOT_FOUND, "Task not found with ID: " + taskId);
        }
        
        // Use assignerId directly instead of assigner.getId()
        return task.getAssignerId() != null && task.getAssignerId().equals(userId);
    }

    /**
     * Check if current user is a manager for the task's scope
     */
    public boolean isTaskManager(UUID taskId) {
        Task task = entityLookupService.getTaskById(taskId);
        if (task == null) {
            throw new FlowXException(FlowXError.NOT_FOUND, "Task not found with ID: " + taskId);
        }
        
        if (task.getTargetType() == Visibility.PROJECT) {
            return hasProjectRole("MANAGER", task.getTargetId());
        } else {
            return isGlobalManager();
        }
    }

    /**
     * Check if current user can create tasks in the given scope
     */
    public boolean canCreateTask(UUID targetId, Visibility targetType) {
        if (targetType == Visibility.GLOBAL) {
            return isGlobalManager();
        }
        if (targetType == Visibility.PROJECT) {
            return hasProjectRole("MANAGER", targetId);
        }
        return false; // No access for other types
    }

    /**
     * Check if current user can access the task (view/edit)
     */
    public boolean canAccessTask(UUID taskId) {
        // Global managers can access any task
        if (isGlobalManager()) {
            return true;
        }
        
        // Check if the user is the assignee or assigner
        if (isTaskAssigner(taskId) || isTaskAssignee(taskId)) {
            return true;
        }
        
        // Check if user is a manager for the task's scope
        return isTaskManager(taskId);
    }

    private UUID getUserId() {
        var context = SecurityContextHolder.getContext();
        if (context.getAuthentication() == null || context.getAuthentication().getPrincipal() == null) {
            throw new FlowXException(FlowXError.UNAUTHENTICATED, "No authenticated user found");
        }

        UserPrincipal userPrincipal = (UserPrincipal) context.getAuthentication().getPrincipal();
        return userPrincipal.getId();
    }
}