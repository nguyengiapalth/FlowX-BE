package project.ii.flowx.model.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import project.ii.flowx.model.entity.Project;
import project.ii.flowx.shared.enums.ProjectStatus;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByDepartmentId(long departmentId);

    List<Project> findByStatus(ProjectStatus status);

    @Query("SELECT DISTINCT p FROM Project p JOIN FETCH p.members m WHERE m.id = ?1")
    List<Project> findByMemberId(Long userId);
}
