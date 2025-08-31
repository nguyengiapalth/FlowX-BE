package project.ii.flowx.applications.jobs;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import project.ii.flowx.module.notify.NotificationService;
import project.ii.flowx.module.manage.service.TaskService;
import project.ii.flowx.module.manage.entity.Task;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@EnableScheduling
public class TaskReminderJob {
    TaskService taskService;
    NotificationService notificationService;

//    @Scheduled(cron = "0 0 8 * * ?") // Every day at 8 AM
//    public void sendTaskReminders() {
//        log.info("Starting task reminder job at {}", java.time.LocalDateTime.now());
//
//        try {
//            // Use the new event-based notification system
//            taskService.sendDueDateReminders();
//            log.info("Task due date reminders sent successfully");
//        } catch (Exception e) {
//            log.error("Failed to send task due date reminders: {}", e.getMessage(), e);
//        }
//
//        log.info("Task reminder job completed.");
//    }
//
//    @Scheduled(cron = "0 0 9,14,17 * * ?") // Every day at 9 AM, 2 PM, and 5 PM
//    public void sendOverdueTaskReminders() {
//        log.info("Starting overdue task reminder job at {}", java.time.LocalDateTime.now());
//
//        try {
//            // Use the new event-based notification system
//            taskService.sendOverdueNotifications();
//            log.info("Overdue task notifications sent successfully");
//        } catch (Exception e) {
//            log.error("Failed to send overdue task notifications: {}", e.getMessage(), e);
//        }
//
//        log.info("Overdue task reminder job completed.");
//    }
//
//    // Additional job for upcoming deadline warnings (3 days, 1 day before due)
//    @Scheduled(cron = "0 0 8 * * ?") // Every day at 8 AM
//    public void sendUpcomingDeadlineWarnings() {
//        log.info("Starting upcoming deadline warning job at {}", java.time.LocalDateTime.now());
//
//        try {
//            // Send warnings for tasks due in 3 days
//            sendUpcomingDeadlineWarning(3);
//
//            // Send warnings for tasks due in 1 day
//            sendUpcomingDeadlineWarning(1);
//
//            log.info("Upcoming deadline warnings sent successfully");
//        } catch (Exception e) {
//            log.error("Failed to send upcoming deadline warnings: {}", e.getMessage(), e);
//        }
//
//        log.info("Upcoming deadline warning job completed.");
//    }
//
//    private void sendUpcomingDeadlineWarning(int daysUntilDue) {
//        List<Task> tasks = taskService.getTasksDueInDays(daysUntilDue);
//
//        for (Task task : tasks) {
//            if (task.getAssigneeId() != null) {
//                taskService.sendTaskDueDateReminder(task.getId(), daysUntilDue);
//            }
//        }
//
//        log.info("Sent {} upcoming deadline warnings for tasks due in {} days", tasks.size(), daysUntilDue);
//    }
}