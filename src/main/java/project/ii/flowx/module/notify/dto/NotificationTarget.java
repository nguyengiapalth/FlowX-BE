package project.ii.flowx.module.notify.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents the target object of a notification
 * This can be any entity that the notification is about (Task, Post, Comment, Project, etc.)
 */
@Schema(description = "Notification Target")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class NotificationTarget {
    
    @Schema(description = "Type of the target entity", example = "TASK")
    String type;
    
    @Schema(description = "ID of the target entity", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID id;
    
    @Schema(description = "Title or name of the target entity", example = "Complete project documentation")
    String title;
    
    @Schema(description = "Description or content of the target entity", example = "Please complete the project documentation by Friday")
    String description;
    
    @Schema(description = "Additional metadata about the target", example = "{\"priority\": \"high\", \"assignee\": \"john.doe@example.com\"}")
    String metadata;
    
    @Schema(description = "Creation timestamp of the target entity")
    LocalDateTime createdAt;
    
    @Schema(description = "Last update timestamp of the target entity")
    LocalDateTime updatedAt;
    
    /**
     * Factory method for creating a task target
     */
    public static NotificationTarget forTask(UUID taskId, String taskTitle, String taskDescription) {
        return NotificationTarget.builder()
                .type("TASK")
                .id(taskId)
                .title(taskTitle)
                .description(taskDescription)
                .build();
    }
    
    /**
     * Factory method for creating a post target
     */
    public static NotificationTarget forPost(UUID postId, String postTitle, String postContent) {
        return NotificationTarget.builder()
                .type("POST")
                .id(postId)
                .title(postTitle)
                .description(postContent)
                .build();
    }
    
    /**
     * Factory method for creating a comment target
     */
    public static NotificationTarget forComment(UUID commentId, String commentContent) {
        return NotificationTarget.builder()
                .type("COMMENT")
                .id(commentId)
                .description(commentContent)
                .build();
    }
    
    /**
     * Factory method for creating a project target
     */
    public static NotificationTarget forProject(UUID projectId, String projectName, String projectDescription) {
        return NotificationTarget.builder()
                .type("PROJECT")
                .id(projectId)
                .title(projectName)
                .description(projectDescription)
                .build();
    }
} 