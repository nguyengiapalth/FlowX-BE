package project.ii.flowx.module.manage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.ii.flowx.module.manage.entity.ProjectMember;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {
    
    /**
     * Find project members by project ID
     */
    List<ProjectMember> findByProjectId(UUID projectId);
    
    /**
     * Find project members by user ID
     */
    List<ProjectMember> findByUserId(UUID userId);

    /**
     * Check if a user is already a member of a project
     */
    boolean existsByProjectIdAndUserId(UUID projectId, UUID userId);
    
    /**
     * Find project member by project ID and user ID
     */
    ProjectMember findByProjectIdAndUserId(UUID projectId, UUID userId);
    
    /**
     * Delete project member by project ID and user ID
     */
    void deleteByProjectIdAndUserId(UUID projectId, UUID userId);
    
    /**
     * Count members in a project
     */
    long countByProjectId(UUID projectId);
}
