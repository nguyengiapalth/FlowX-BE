package project.ii.flowx.applications.jobs;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import project.ii.flowx.applications.service.communicate.NotificationService;
import project.ii.flowx.applications.service.communicate.TaskService;
import project.ii.flowx.model.entity.Task;
import project.ii.flowx.model.entity.User;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@EnableScheduling
public class TaskReminderJob{
     private final TaskService taskService;
     private final NotificationService notificationService;

     @Scheduled(cron = "0 0 8 * * ?") // Every day at 8 AM
    public void sendTaskReminders() {
        log.info("Starting task reminder job at {}", java.time.LocalDateTime.now());
         List<Task> tasksDueToday = taskService.getTasksDueToday();
         // for each user, send a reminder for their tasks
         Map<User, List<Task>> tasksByUser = tasksDueToday.stream()
                 .collect(Collectors.groupingBy(Task::getAssignee));

         tasksByUser.forEach((user, tasks) -> {
             try {
//                 notificationService.sendTaskReminder(user, tasks);
                 log.info("Sent reminder for user: {} with {} tasks", user.getId(), tasks.size());
             } catch (Exception e) {
                 log.error("Failed to send reminder for user: {}", user.getId(), e);
             }
         });

        log.info("Task reminder job completed.");
    }

    @Scheduled(fixedRate = 600000) // Every 10 minutes
    public void sendOverdueTaskReminders() {
        log.info("Starting overdue task reminder job at {}", java.time.LocalDateTime.now());
        List<Task> overdueTasks = taskService.getOverdueTasks();

        // for each user, send a reminder for their overdue tasks
        Map<User, List<Task>> tasksByUser = overdueTasks.stream()
                .collect(Collectors.groupingBy(Task::getAssignee));

        tasksByUser.forEach((user, tasks) -> {
            try {
//                notificationService.sendOverdueTaskReminder(user, tasks);
                log.info("Sent overdue task reminder for user: {} with {} tasks", user.getId(), tasks.size());
            } catch (Exception e) {
                log.error("Failed to send overdue task reminder for user: {}", user.getId(), e);
            }
        });
    }


}