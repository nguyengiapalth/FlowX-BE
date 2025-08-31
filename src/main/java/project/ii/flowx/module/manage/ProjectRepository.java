package project.ii.flowx.module.manage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.ii.flowx.module.manage.entity.Project;
import project.ii.flowx.applications.enums.ProjectStatus;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    /**
     * Find projects by status
     */
    List<Project> findByStatus(ProjectStatus status);

    /**
     * Find projects where a user is a member using ProjectMember relationship
     */
    @Query("SELECT DISTINCT p FROM Project p JOIN ProjectMember pm ON p.id = pm.project.id WHERE pm.userId = :userId")
    List<Project> findByMemberId(@Param("userId") UUID userId);
    
    /**
     * Find projects by name containing (case insensitive)
     */
    @Query("SELECT p FROM Project p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Project> findByNameContainingIgnoreCase(@Param("name") String name);
    
    /**
     * Find projects by status and member user ID
     */
    @Query("SELECT DISTINCT p FROM Project p JOIN ProjectMember pm ON p.id = pm.project.id WHERE p.status = :status AND pm.userId = :userId")
    List<Project> findByStatusAndMemberId(@Param("status") ProjectStatus status, @Param("userId") UUID userId);
}
