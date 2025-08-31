package project.ii.flowx.module.auth.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ii.flowx.applications.helper.EntityLookupService;
import project.ii.flowx.module.auth.entity.Role;
import project.ii.flowx.module.auth.entity.UserRole;
import project.ii.flowx.module.auth.dto.userrole.UserRoleCreateRequest;
import project.ii.flowx.module.auth.dto.userrole.UserRoleResponse;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;
import project.ii.flowx.module.auth.repository.UserRoleRepository;
import project.ii.flowx.module.user.repository.UserRepository;
import project.ii.flowx.module.auth.mapper.UserRoleMapper;
import project.ii.flowx.applications.enums.RoleScope;

import java.util.List;
import java.util.UUID;

@Service()
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserRoleService {
    UserRoleRepository userRoleRepository;
    UserRoleMapper userRoleMapper;
    RoleService roleService;
    UserRepository userRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "roles", key = "#userId", unless = "#result == null || #result.isEmpty()")
    public List<UserRoleResponse> getRolesForUser(UUID userId) {
        List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
        log.info("getRolesForUser {} roles {} in user role service, not in cache", userId, userRoles);
        return userRoleMapper.toUserRoleResponseList(userRoles);
    }

    @Transactional(readOnly = true)
    public List<UserRoleResponse> getGlobalRolesForUser(UUID userId) {
        List<UserRole> userRoles = userRoleRepository.findGlobalRoleByUserId(userId);
        return userRoleMapper.toUserRoleResponseList(userRoles);
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_HR') " +
            "or @authorize.canAssignRole(#userRoleCreateRequest.scope, #userRoleCreateRequest.scopeId)")
    @CacheEvict(value = "roles", key = "#userRoleCreateRequest.userId")
    public void assignRoleToUser(UserRoleCreateRequest userRoleCreateRequest) {
        UUID userId = userRoleCreateRequest.getUserId();
        UUID roleId = userRoleCreateRequest.getRoleId();
        
        // Validate userId exists
        if (!userRepository.existsById(userId)) {
            throw new FlowXException(FlowXError.NOT_FOUND, "User not found with ID: " + userId);
        }
        
        // Validate and get role
        Role role = roleService.getRoleById(roleId);

        // Validate scope requirements
        if (userRoleCreateRequest.getScope() != RoleScope.GLOBAL) {
            if (userRoleCreateRequest.getScopeId() == null) {
                throw new FlowXException(FlowXError.BAD_REQUEST, "Scope ID must be provided for non-global roles");
            }
            
            // Validate user has member role in the scope, if role != member
            if (!role.getName().equals("MEMBER")) {
                if (!userRoleRepository.existsByUserIdAndScopeAndScopeId(
                        userId, 
                        userRoleCreateRequest.getScope(), 
                        userRoleCreateRequest.getScopeId())) {
                    throw new FlowXException(FlowXError.BAD_REQUEST, 
                            "User must have a member role in the specified scope");
                }
            }
        }

        // Check if user already has this role in this scope
        if (userRoleRepository.existsByUserIdAndRoleIdAndScopeAndScopeId(
                userId, roleId, userRoleCreateRequest.getScope(), userRoleCreateRequest.getScopeId())) {
            throw new FlowXException(FlowXError.CONFLICT, 
                    "User already has this role in the specified scope");
        }

        // Create UserRole using mapper and set the role
        UserRole userRole = userRoleMapper.toUserRole(userRoleCreateRequest);
        userRole.setUserId(userId); // Set userId explicitly
        userRole.setRole(role); // Set role object after mapping
        
        userRoleRepository.save(userRole);
        log.info("Assigned role {} to user {} in scope {}", role.getName(), userId, userRoleCreateRequest.getScope());
    }

    @Transactional
    @CacheEvict(value = "roles", key = "#userId")
    public void deleteUserRoleByUserIdAndRoleIdAndScope(UUID userId, UUID roleId, RoleScope roleScope, UUID scopeId) {
        if (!userRoleRepository.existsByUserIdAndRoleIdAndScopeAndScopeId(userId, roleId, roleScope, scopeId)) {
            throw new FlowXException(FlowXError.NOT_FOUND, "User role not found");
        }
        
        userRoleRepository.deleteByUserIdAndRoleIdAndScopeAndScopeId(userId, roleId, roleScope, scopeId);
        log.info("Deleted role {} from user {} in scope {}", roleId, userId, roleScope);
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_HR')")
    @CacheEvict(value = "roles", key = "#userId")
    public void deleteUserRolesByUserIdAndScope(UUID userId, RoleScope roleScope, UUID scopeId) {
        userRoleRepository.deleteByUserIdAndScopeAndScopeId(userId, roleScope, scopeId);
        log.info("Deleted all roles for user {} in scope {}", userId, roleScope);
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_HR')")
    @CacheEvict(value = "roles", allEntries = true)
    public void deleteUserRolesByScope(RoleScope roleScope, UUID scopeId) {
        userRoleRepository.deleteByScopeAndScopeId(roleScope, scopeId);
        log.info("Deleted all user roles in scope {} with ID {}", roleScope, scopeId);
    }
}