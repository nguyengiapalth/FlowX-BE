package project.ii.flowx.module.manage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.ii.flowx.module.manage.entity.Task;
import project.ii.flowx.applications.enums.Visibility;
import project.ii.flowx.applications.enums.TaskStatus;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    
    /**
     * Find tasks assigned to a specific user
     */
    List<Task> findByAssigneeId(UUID userId);

    /**
     * Find tasks assigned by a specific user
     */
    List<Task> findByAssignerId(UUID userId);

    /**
     * Find tasks assigned to a user with specific status
     */
    List<Task> findByAssigneeIdAndStatus(UUID userId, TaskStatus status);

    /**
     * Find tasks by status
     */
    List<Task> findByStatus(TaskStatus status);

    /**
     * Find tasks assigned to a user for a specific target
     */
    List<Task> findByAssigneeIdAndTargetTypeAndTargetId(UUID userId, Visibility visibility, UUID targetId);

    /**
     * Find tasks by target type and target ID
     */
    List<Task> findByTargetTypeAndTargetId(Visibility visibility, UUID targetId);

    /**
     * Find tasks due today
     */
    @Query("SELECT t FROM Task t WHERE t.dueDate = CURRENT_DATE")
    List<Task> findTasksDueToday();

    /**
     * Find overdue tasks (past due date and not completed)
     */
    @Query("SELECT t FROM Task t WHERE t.dueDate < CURRENT_DATE AND t.status <> 'COMPLETED'")
    List<Task> findOverdueTasks();

    /**
     * Find tasks due in specified number of days
     */
    @Query("SELECT t FROM Task t WHERE t.dueDate = DATEADD(DAY, :days, CURRENT_DATE) AND t.status <> 'COMPLETED'")
    List<Task> findTasksDueInDays(@Param("days") int days);
}
