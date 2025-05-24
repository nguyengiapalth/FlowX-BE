package project.ii.flowx.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.ii.flowx.model.entity.Task;
import project.ii.flowx.shared.enums.TaskStatus;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByAssigneeId(Long userId);

    List<Task> findByAssignerId(Long userId);

    List<Task> findByProjectId(Long projectId);
    
    List<Task> findByDepartmentId(Long departmentId);

    List<Task> findByAssigneeIdAndProjectId(Long userId, Long projectId);

    List<Task> findByAssigneeIdAndStatus(Long userId, TaskStatus status);

    List<Task> findByStatus(TaskStatus status);
}
