package project.ii.flowx.model.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.ii.flowx.model.entity.Task;
import project.ii.flowx.shared.enums.ContentTargetType;
import project.ii.flowx.shared.enums.TaskStatus;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByAssigneeId(Long userId);

    List<Task> findByAssignerId(Long userId);

    List<Task> findByAssigneeIdAndStatus(Long userId, TaskStatus status);

    List<Task> findByStatus(TaskStatus status);

    List<Task> findByAssigneeIdAndTargetTypeAndTargetId(Long userId, ContentTargetType contentTargetType, Long targetId);

    @EntityGraph(attributePaths = {"assignee", "assigner"})
    List<Task> findByTargetTypeAndTargetId(ContentTargetType contentTargetType, Long targetId);

    @EntityGraph(attributePaths = {"assignee", "assigner"})
    @Query("SELECT t FROM Task t WHERE t.dueDate = CURRENT_DATE")
    List<Task> findTasksDueToday();

    @EntityGraph(attributePaths = {"assignee", "assigner"})
    @Query("SELECT t FROM Task t WHERE t.dueDate < CURRENT_DATE AND t.status <> 'COMPLETED'")
    List<Task> findOverdueTasks();

    @EntityGraph(attributePaths = {"assignee", "assigner"})
    @Query("SELECT t FROM Task t WHERE t.dueDate = DATEADD(DAY, :days, CURRENT_DATE) AND t.status <> 'COMPLETED'")
    List<Task> findTasksDueInDays(@Param("days") int days);
}
