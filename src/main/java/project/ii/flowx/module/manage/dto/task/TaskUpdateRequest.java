package project.ii.flowx.module.manage.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import project.ii.flowx.applications.enums.PriorityLevel;
import project.ii.flowx.applications.enums.TaskStatus;

import java.time.LocalDate;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Schema(description = "Task Update Request")
public class TaskUpdateRequest {
    @Size(min = 2, max = 200, message = "Task title must be between 2 and 200 characters")
    @Schema(description = "Title of the task", example = "Implement user authentication with OAuth")
    String title;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Schema(description = "Description of the task", example = "Implement OAuth-based authentication for users")
    String description;

    @Schema(description = "Start date of the task", example = "2023-01-01")
    LocalDate startDate;

    @Future(message = "Due date must be in the future")
    @Schema(description = "Due date of the task", example = "2023-01-15")
    LocalDate dueDate;

    @Schema(description = "Status of the task", example = "IN_PROGRESS")
    TaskStatus status;

    @Schema(description = "Priority level of the task", example = "HIGH")
    PriorityLevel priority;
}