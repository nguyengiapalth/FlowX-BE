package project.ii.flowx.applications.service.auth;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ii.flowx.applications.service.helper.EntityLookupService;
import project.ii.flowx.model.entity.Role;
import project.ii.flowx.model.entity.User;
import project.ii.flowx.model.entity.UserRole;
import project.ii.flowx.model.dto.userrole.UserRoleCreateRequest;
import project.ii.flowx.model.dto.userrole.UserRoleResponse;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;
import project.ii.flowx.model.repository.UserRoleRepository;
import project.ii.flowx.model.mapper.UserRoleMapper;
import project.ii.flowx.shared.enums.RoleScope;

import java.util.List;

@Service()
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserRoleService {
    UserRoleRepository userRoleRepository;
    UserRoleMapper userRoleMapper;
    EntityLookupService entityLookupService;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_HR')")
    public List<UserRoleResponse> getRolesForUser(Long userId) {
        List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
        log.info("User roles for user with id {} : {}", userId, userRoles);
        return userRoleMapper.toUserRoleResponseList(userRoles);
    }

    @Transactional(readOnly = true)
    public List<UserRoleResponse> getGlobalRolesForUser(Long userId) {
        List<UserRole> userRoles = userRoleRepository.findGlobalRoleByUserId(userId); // Assuming this method exists
        return userRoleMapper.toUserRoleResponseList(userRoles);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_HR') or #userId == authentication.principal.id")
    @Cacheable(value = "userLocalRoles", key = "#userId")
    public List<UserRoleResponse> getNonGlobalRolesForUser(Long userId) {
        List<UserRole> userRoles = userRoleRepository.findLocalRoleByUserId(userId);

        log.info("Non-global user roles for user with id {} : {}", userId, userRoles);
        return userRoleMapper.toUserRoleResponseList(userRoles);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('ROLE_DEVELOPER', 'ROLE_MANAGER', 'ROLE_HR')")
    @Cacheable(value = "roleUsersByRoleId", key = "#roleId")
    public List<UserRoleResponse> getUsersForRole(Long roleId) {
        List<UserRole> userRoles = userRoleRepository.findByRoleId(roleId);
        log.info("User roles for role with id {} : {}", roleId, userRoles);
        return userRoleMapper.toUserRoleResponseList(userRoles);
    }

    @Transactional
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('MANAGER')")
    @CacheEvict(value = {"userLocalRoles"}, key = "#userRoleCreateRequest.userId")
    public void assignRoleToUser(UserRoleCreateRequest userRoleCreateRequest) {
        // validate userId and roleId
        User user = entityLookupService.getUserById(userRoleCreateRequest.getUserId());
        Role role = entityLookupService.getRoleById(userRoleCreateRequest.getRoleId());

        if (userRoleCreateRequest.getScope() != RoleScope.GLOBAL) {
            // Validate scopeId
            if (userRoleCreateRequest.getScopeId() == null || userRoleCreateRequest.getScopeId() <= 0)
                throw new FlowXException(FlowXError.BAD_REQUEST, "Scope ID must be provided for non-global roles");

            // validate user has member userrole in the scope
            // if role = member, we don't need to check this
            if (role.getName().equals("MEMBER"))
                log.info("Assigning member role to user {}", user.getEmail());
            else if (!userRoleRepository.existsByUserIdAndScopeAndScopeId(user.getId(), userRoleCreateRequest.getScope(), userRoleCreateRequest.getScopeId()))
                throw new FlowXException(FlowXError.BAD_REQUEST, "User must have a member role in the specified scope");
        }

        UserRole userRole = userRoleMapper.toUserRole(userRoleCreateRequest);
        userRoleRepository.save(userRole);

        log.info("Assigned role {} to user {}, cache evicted", role.getName(), user.getEmail());
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_HR')")
    public void deleteUserRole(Long id) {
        UserRole userRole = userRoleRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "User role not found"));

        Long userId = userRole.getUser().getId();
        userRoleRepository.deleteById(id);
        log.info("Deleted user role id {}, cache evicted for user {}", id, userId);
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_HR')")
    @CacheEvict(value = {"userLocalRoles"}, key = "#userId")
    public void deleteUserRolesByUserIdAndScope(Long userId, RoleScope roleScope, Long scopeId) {
        userRoleRepository.deleteByUserIdAndScopeAndScopeId(userId, roleScope, scopeId);

        // Evict cache after successful deletion
        log.info("Deleted user roles for user {} with scope {} and scopeId {}, cache evicted",
                userId, roleScope, scopeId);
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_HR')")
//    @CacheEvict(value = {"userLocalRoles"}, key = "#userId")
    public void deleteUserRolesByScope(RoleScope roleScope, long scopeId) {
        userRoleRepository.deleteByScopeAndScopeId(roleScope, scopeId);
        log.info("Deleted user roles with scope {} and scopeId {}, cache evicted", roleScope, scopeId);
    }

}