package project.ii.flowx.module.auth.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import project.ii.flowx.module.auth.entity.UserRole;
import project.ii.flowx.applications.enums.RoleScope;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {

    /**
     * Find global roles for a user
     */
    @EntityGraph(attributePaths = {"role"})
    @Query("SELECT ur FROM UserRole ur WHERE ur.userId = :userId AND ur.scope = 'GLOBAL'")
    List<UserRole> findGlobalRoleByUserId(UUID userId);

    /**
     * Find local (non-global) roles for a user
     */
    @EntityGraph(attributePaths = {"role"})
    @Query("SELECT ur FROM UserRole ur WHERE ur.userId = :userId AND ur.scope != 'GLOBAL'")
    List<UserRole> findLocalRoleByUserId(UUID userId);

    /**
     * Find all roles for a user with role details
     */
    @EntityGraph(attributePaths = {"role"})
    List<UserRole> findByUserId(UUID userId);

    /**
     * Delete user roles by user, scope and scope ID
     */
    void deleteByUserIdAndScopeAndScopeId(UUID userId, RoleScope roleScope, UUID scopeId);

    /**
     * Delete user role by user, role, scope and scope ID
     */
    void deleteByUserIdAndRoleIdAndScopeAndScopeId(UUID userId, UUID roleId, RoleScope roleScope, UUID scopeId);

    /**
     * Delete all user roles by scope and scope ID
     */
    void deleteByScopeAndScopeId(RoleScope roleScope, UUID scopeId);

    /**
     * Check if role exists in scope
     */
    boolean existsByRoleIdAndScopeAndScopeId(UUID roleId, RoleScope roleScope, UUID scopeId);

    /**
     * Check if user has role in scope
     */
    boolean existsByUserIdAndScopeAndScopeId(UUID userId, RoleScope scope, UUID scopeId);

    /**
     * Check if user has specific role in scope
     */
    boolean existsByUserIdAndRoleIdAndScopeAndScopeId(UUID userId, UUID roleId, RoleScope scope, UUID scopeId);
}
