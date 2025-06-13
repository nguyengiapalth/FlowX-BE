package project.ii.flowx.applications.eventhandlers;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import project.ii.flowx.applications.events.TaskEvent;
import project.ii.flowx.applications.service.auth.UserRoleService;
import project.ii.flowx.applications.service.communicate.NotificationService;
import project.ii.flowx.applications.service.helper.EntityLookupService;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;
import project.ii.flowx.model.dto.notification.NotificationCreateRequest;
import project.ii.flowx.model.entity.Task;
import project.ii.flowx.model.entity.User;
import project.ii.flowx.model.repository.TaskRepository;
import project.ii.flowx.shared.enums.ContentTargetType;

import java.util.List;

@Component
@EnableAsync
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TaskEventHandler {
    UserRoleService userRoleService;
    NotificationService notificationService;
    EntityLookupService entityLookupService;
    TaskRepository taskRepository;

    @EventListener
    @Async
    public void handleTaskCreatedEvent(TaskEvent.TaskCreatedEvent event) {
        log.info("Task created event: {}", event);
        
        try {
            Task task = taskRepository.findById(event.taskId())
                    .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Task not found"));
            
            User assigner = entityLookupService.getUserById(event.userId());
            
            // Notify relevant users about new task creation
            List<User> recipients = getTaskNotificationRecipients(task);
            
            for (User recipient : recipients) {
                if (!recipient.getId().equals(event.userId())) { // Don't notify the creator
                    NotificationCreateRequest notificationRequest = NotificationCreateRequest.builder()
                            .userId(recipient.getId())
                            .title("Task mới được tạo")
                            .content(String.format("%s đã tạo task mới: \"%s\"", 
                                    assigner.getFullName(), 
                                    truncateText(task.getTitle(), 80)))
                            .entityType("TASK")
                            .entityId(event.taskId())
                            .build();
                    
                    notificationService.createNotification(notificationRequest);
                }
            }
        } catch (Exception e) {
            log.error("Error handling task created event: {}", e.getMessage(), e);
        }
    }

    @EventListener
    @Async
    public void handleTaskAssignedEvent(TaskEvent.TaskAssignedEvent event) {
        log.info("Task assigned event: {}", event);
        
        try {
            Task task = taskRepository.findById(event.taskId())
                    .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Task not found"));
            
                         User assignee = entityLookupService.getUserById(event.assigneeId());
             User assigner = entityLookupService.getUserById(event.assignerId());
            
            // Notify the assignee about the new task assignment
            if (!assignee.getId().equals(assigner.getId())) { // Don't notify if self-assigned
                NotificationCreateRequest notificationRequest = NotificationCreateRequest.builder()
                        .userId(assignee.getId())
                        .title("Bạn được giao task mới")
                        .content(String.format("%s đã giao cho bạn task: \"%s\"", 
                                assigner.getFullName(), 
                                truncateText(task.getTitle(), 80)))
                        .entityType("TASK")
                        .entityId(event.taskId())
                        .build();
                
                notificationService.createNotification(notificationRequest);
            }
            
            // Notify team members about the assignment (optional)
            List<User> teamMembers = getTaskNotificationRecipients(task);
            for (User member : teamMembers) {
                if (!member.getId().equals(assignee.getId()) && 
                    !member.getId().equals(assigner.getId())) {
                    
                    NotificationCreateRequest notificationRequest = NotificationCreateRequest.builder()
                            .userId(member.getId())
                            .title("Task được gán cho thành viên")
                            .content(String.format("Task \"%s\" đã được gán cho %s", 
                                    truncateText(task.getTitle(), 80),
                                    assignee.getFullName()))
                            .entityType("TASK")
                            .entityId(event.taskId())
                            .build();
                    
                    notificationService.createNotification(notificationRequest);
                }
            }
        } catch (Exception e) {
            log.error("Error handling task assigned event: {}", e.getMessage(), e);
        }
    }

    @EventListener
    @Async
    public void handleTaskUnassignedEvent(TaskEvent.TaskUnassignedEvent event) {
        log.info("Task unassigned event: {}", event);
        
        try {
            Task task = taskRepository.findById(event.taskId())
                    .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Task not found"));
            
                         User previousAssignee = entityLookupService.getUserById(event.previousAssigneeId());
            
            // Notify the previous assignee about unassignment
            NotificationCreateRequest notificationRequest = NotificationCreateRequest.builder()
                    .userId(previousAssignee.getId())
                    .title("Task đã được hủy gán")
                    .content(String.format("Bạn không còn được gán task: \"%s\"", 
                            truncateText(task.getTitle(), 80)))
                    .entityType("TASK")
                    .entityId(event.taskId())
                    .build();
            
            notificationService.createNotification(notificationRequest);
        } catch (Exception e) {
            log.error("Error handling task unassigned event: {}", e.getMessage(), e);
        }
    }

    @EventListener
    @Async
    public void handleTaskUpdatedEvent(TaskEvent.TaskUpdatedEvent event) {
        log.info("Task updated event: {}", event);
        
        try {
            Task task = taskRepository.findById(event.taskId())
                    .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Task not found"));
            
            User updater = entityLookupService.getUserById(event.userId());
            
            // Notify assignee about task update (if not the same person who updated)
            if (task.getAssignee() != null && !task.getAssignee().getId().equals(event.userId())) {
                NotificationCreateRequest notificationRequest = NotificationCreateRequest.builder()
                        .userId(task.getAssignee().getId())
                        .title("Task được cập nhật")
                        .content(String.format("%s đã cập nhật task: \"%s\"", 
                                updater.getFullName(), 
                                truncateText(task.getTitle(), 80)))
                        .entityType("TASK")
                        .entityId(event.taskId())
                        .build();
                
                notificationService.createNotification(notificationRequest);
            }
            
            // Notify assigner about task update (if different from updater and assignee)
            if (task.getAssigner() != null && 
                !task.getAssigner().getId().equals(event.userId()) &&
                (task.getAssignee() == null || !task.getAssigner().getId().equals(task.getAssignee().getId()))) {
                
                NotificationCreateRequest notificationRequest = NotificationCreateRequest.builder()
                        .userId(task.getAssigner().getId())
                        .title("Task bạn giao được cập nhật")
                        .content(String.format("%s đã cập nhật task: \"%s\"", 
                                updater.getFullName(), 
                                truncateText(task.getTitle(), 80)))
                        .entityType("TASK")
                        .entityId(event.taskId())
                        .build();
                
                notificationService.createNotification(notificationRequest);
            }
        } catch (Exception e) {
            log.error("Error handling task updated event: {}", e.getMessage(), e);
        }
    }

    @EventListener
    @Async
    public void handleTaskCompletedEvent(TaskEvent.TaskCompletedEvent event) {
        log.info("Task completed event: {}", event);
        
        try {
            Task task = taskRepository.findById(event.taskId())
                    .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Task not found"));
            
            User completer = entityLookupService.getUserById(event.userId());
            
            // Notify assigner about task completion
            if (task.getAssigner() != null && !task.getAssigner().getId().equals(event.userId())) {
                NotificationCreateRequest notificationRequest = NotificationCreateRequest.builder()
                        .userId(task.getAssigner().getId())
                        .title("Task hoàn thành")
                        .content(String.format("%s đã hoàn thành task: \"%s\"", 
                                completer.getFullName(), 
                                truncateText(task.getTitle(), 80)))
                        .entityType("TASK")
                        .entityId(event.taskId())
                        .build();
                
                notificationService.createNotification(notificationRequest);
            }
            
            // Notify team members about task completion
            List<User> teamMembers = getTaskNotificationRecipients(task);
            for (User member : teamMembers) {
                if (!member.getId().equals(event.userId()) && 
                    (task.getAssigner() == null || !member.getId().equals(task.getAssigner().getId()))) {
                    
                    NotificationCreateRequest notificationRequest = NotificationCreateRequest.builder()
                            .userId(member.getId())
                            .title("Task trong nhóm hoàn thành")
                            .content(String.format("Task \"%s\" đã được hoàn thành bởi %s", 
                                    truncateText(task.getTitle(), 80),
                                    completer.getFullName()))
                            .entityType("TASK")
                            .entityId(event.taskId())
                            .build();
                    
                    notificationService.createNotification(notificationRequest);
                }
            }
        } catch (Exception e) {
            log.error("Error handling task completed event: {}", e.getMessage(), e);
        }
    }

    @EventListener
    @Async
    public void handleTaskDeletedEvent(TaskEvent.TaskDeletedEvent event) {
        log.info("Task deleted event: {}", event);
        // This is mainly for cleanup and logging purposes
        // Could potentially notify affected users if needed
    }

    @EventListener
    @Async
    public void handleTaskDueDateReminderEvent(TaskEvent.TaskDueDateReminderEvent event) {
        log.info("Task due date reminder event: {}", event);
        
        try {
            User assignee = entityLookupService.getUserById(event.assigneeId());
            
            String title = event.daysUntilDue() == 0 ? "Task hết hạn hôm nay" : 
                          String.format("Task sắp hết hạn trong %d ngày", event.daysUntilDue());
            
            String content = event.daysUntilDue() == 0 ? 
                           String.format("Task \"%s\" hết hạn hôm nay. Hãy hoàn thành sớm!", truncateText(event.taskTitle(), 80)) :
                           String.format("Task \"%s\" sẽ hết hạn trong %d ngày", truncateText(event.taskTitle(), 80), event.daysUntilDue());
            
            NotificationCreateRequest notificationRequest = NotificationCreateRequest.builder()
                    .userId(assignee.getId())
                    .title(title)
                    .content(content)
                    .entityType("TASK")
                    .entityId(event.taskId())
                    .build();
            
            notificationService.createNotification(notificationRequest);
        } catch (Exception e) {
            log.error("Error handling task due date reminder event: {}", e.getMessage(), e);
        }
    }

    @EventListener
    @Async
    public void handleTaskOverdueEvent(TaskEvent.TaskOverdueEvent event) {
        log.info("Task overdue event: {}", event);
        
        try {
            User assignee = entityLookupService.getUserById(event.assigneeId());
            
            NotificationCreateRequest notificationRequest = NotificationCreateRequest.builder()
                    .userId(assignee.getId())
                    .title("Task quá hạn")
                    .content(String.format("Task \"%s\" đã quá hạn %d ngày. Vui lòng hoàn thành ngay!", 
                            truncateText(event.taskTitle(), 80), 
                            event.daysOverdue()))
                    .entityType("TASK")
                    .entityId(event.taskId())
                    .build();
            
            notificationService.createNotification(notificationRequest);
        } catch (Exception e) {
            log.error("Error handling task overdue event: {}", e.getMessage(), e);
        }
    }

    private List<User> getTaskNotificationRecipients(Task task) {
        // Get notification recipients based on task target type
        if (task.getTargetType() == ContentTargetType.DEPARTMENT) {
            return entityLookupService.getUsersByDepartmentId(task.getTargetId());
        } else if (task.getTargetType() == ContentTargetType.PROJECT) {
            return entityLookupService.getUsersByProjectId(task.getTargetId());
        } else {
            // For GLOBAL tasks, return empty list or implement specific logic
            return List.of();
        }
    }
    
    private String truncateText(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }
}
