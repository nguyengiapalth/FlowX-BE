package project.ii.flowx.module.manage.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import project.ii.flowx.applications.enums.Visibility;
import project.ii.flowx.applications.enums.PriorityLevel;
import project.ii.flowx.applications.enums.TaskStatus;

import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Task Create Request")
@Data
@Getter
@Setter
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class TaskCreateRequest {
    @NotBlank(message = "Task title is required")
    @Size(min = 2, max = 200, message = "Task title must be between 2 and 200 characters")
    @Schema(description = "Title of the task", example = "Implement user authentication")
    String title;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Schema(description = "Description of the task", example = "Implement JWT-based authentication for users")
    String description;

    @NotNull(message = "Target type is required")
    @Enumerated(EnumType.STRING)
    @Schema(description = "Type of the entity involved in the task", example = "PROJECT")
    Visibility targetType;

    @NotNull(message = "Target ID is required")
    @Schema(description = "ID of the target entity for the task", example = "1")
    UUID targetId;

    @NotNull(message = "Assigner ID is required")
    @Schema(description = "ID of the user who assigned the task", example = "3")
    UUID assignerId;

    @NotNull(message = "Assignee ID is required")
    @Schema(description = "ID of the user who is assigned to the task", example = "4")
    UUID assigneeId;

    @NotNull(message = "Start date is required")
    @Schema(description = "Start date of the task", example = "2023-01-01")
    LocalDate startDate;

    @Future(message = "Due date must be in the future")
    @Schema(description = "Due date of the task", example = "2023-01-15")
    LocalDate dueDate;

    @Schema(description = "Whether the task has files attached", example = "false")
    Boolean hasFiles;

    @NotNull(message = "Status is required")
    @Schema(description = "Status of the task", example = "TO_DO")
    TaskStatus status;

    @NotNull(message = "Priority is required")
    @Schema(description = "Priority level of the task", example = "MEDIUM")
    PriorityLevel priority;
}
