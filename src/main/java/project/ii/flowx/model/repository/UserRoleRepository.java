package project.ii.flowx.model.repository;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import project.ii.flowx.model.entity.UserRole;
import project.ii.flowx.shared.enums.RoleScope;

import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    @EntityGraph(attributePaths = {"user"})
    List<UserRole> findByRoleId(Long roleId);

    @EntityGraph(attributePaths = {"role"})
    List<UserRole> findByUserId(Long userId);

    // find global role for user
    @Query("SELECT ur FROM UserRole ur JOIN FETCH ur.role WHERE ur.user.id = ?1 AND ur.scope = 'GLOBAL'")
    List<UserRole> findGlobalRoleByUserId(Long userId);

    @EntityGraph(attributePaths = {"role"})
    @Query("SELECT ur FROM UserRole ur JOIN FETCH ur.role WHERE ur.user.id = ?1 AND ur.scope != 'GLOBAL'")
    List<UserRole> findLocalRoleByUserId(Long userId);

    void deleteByUserIdAndScopeAndScopeId(Long userId, RoleScope roleScope, Long scopeId);

    boolean existsByRoleIdAndScopeAndScopeId(Long id, RoleScope roleScope, Long departmentId);

    void deleteByScopeAndScopeId(RoleScope roleScope, long projectId);

    boolean existsByUserIdAndScopeAndScopeId(Long id, RoleScope scope, Long scopeId);

    void deleteByUserIdAndRoleIdAndScopeAndScopeId(Long userId, Long roleId, RoleScope roleScope, Long scopeId);
}
