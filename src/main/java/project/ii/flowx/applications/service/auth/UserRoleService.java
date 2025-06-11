package project.ii.flowx.applications.service.auth;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
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
import project.ii.flowx.security.UserPrincipal;
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
    public List<UserRoleResponse> getRolesForUser(Long userId) {
        List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
        return userRoleMapper.toUserRoleResponseList(userRoles);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<UserRoleResponse> getMyRoles() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userPrincipal.getId();

        List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
        return userRoleMapper.toUserRoleResponseList(userRoles);
    }

    @Transactional(readOnly = true)
    public List<UserRoleResponse> getGlobalRolesForUser(Long userId) {
        List<UserRole> userRoles = userRoleRepository.findGlobalRoleByUserId(userId); // Assuming this method exists
        return userRoleMapper.toUserRoleResponseList(userRoles);
    }

    @Transactional(readOnly = true)
//    @Cacheable(value = "userLocalRoles", key = "#userId")// , cacheManager = "caffeineCacheManager")
    public List<UserRoleResponse> getNonGlobalRolesForUser(Long userId) {
        List<UserRole> userRoles = userRoleRepository.findLocalRoleByUserId(userId);
        return userRoleMapper.toUserRoleResponseList(userRoles);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_HR')")
    public List<UserRoleResponse> getUsersForRole(Long roleId) {
        List<UserRole> userRoles = userRoleRepository.findByRoleId(roleId);
        return userRoleMapper.toUserRoleResponseList(userRoles);
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_HR') " +
            "or @authorize.canAssignRole(#userRoleCreateRequest.scope, #userRoleCreateRequest.scopeId)")
//    @CacheEvict(value = {"userLocalRoles"}, key = "#userRoleCreateRequest.userId")//, cacheManager = "caffeineCacheManager")
    public void assignRoleToUser(UserRoleCreateRequest userRoleCreateRequest) {
        // validate userId and roleId
        User user = entityLookupService.getUserById(userRoleCreateRequest.getUserId());
        Role role = entityLookupService.getRoleById(userRoleCreateRequest.getRoleId());

        if (userRoleCreateRequest.getScope() != RoleScope.GLOBAL) {
            if (userRoleCreateRequest.getScopeId() == null || userRoleCreateRequest.getScopeId() <= 0)
                throw new FlowXException(FlowXError.BAD_REQUEST, "Scope ID must be provided for non-global roles");
            // validate user has member userrole in the scope
            // if role = member, we don't need to check this
            if (!role.getName().equals("MEMBER")) {
                if (!userRoleRepository.existsByUserIdAndScopeAndScopeId(user.getId(), userRoleCreateRequest.getScope(), userRoleCreateRequest.getScopeId()))
                    throw new FlowXException(FlowXError.BAD_REQUEST, "User must have a member role in the specified scope");
            }
        }

        UserRole userRole = userRoleMapper.toUserRole(userRoleCreateRequest);
        userRoleRepository.save(userRole);

    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_HR')")
    public void deleteUserRole(Long id) {
        UserRole userRole = userRoleRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "User role not found"));

        Long userId = userRole.getUser().getId();
        userRoleRepository.deleteById(id);
    }

    public void deleteUserRoleByUserIdAndRoleIdAndScope(Long userId, Long roleId, RoleScope roleScope, Long scopeId) {
        userRoleRepository.deleteByUserIdAndRoleIdAndScopeAndScopeId(userId, roleId, roleScope, scopeId);
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_HR')")
//    @CacheEvict(value = {"userLocalRoles"}, key = "#userId")//, cacheManager = "caffeineCacheManager")
    public void deleteUserRolesByUserIdAndScope(Long userId, RoleScope roleScope, Long scopeId) {
        userRoleRepository.deleteByUserIdAndScopeAndScopeId(userId, roleScope, scopeId);
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_HR')")
    public void deleteUserRolesByScope(RoleScope roleScope, long scopeId) {
        userRoleRepository.deleteByScopeAndScopeId(roleScope, scopeId);
    }

}