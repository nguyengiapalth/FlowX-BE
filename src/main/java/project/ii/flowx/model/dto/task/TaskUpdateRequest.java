package project.ii.flowx.model.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import project.ii.flowx.shared.enums.PriorityLevel;
import project.ii.flowx.shared.enums.TaskStatus;

import java.time.LocalDate;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Schema(description = "Task Update Request")
public class TaskUpdateRequest {
    @Schema(description = "Title of the task", example = "Implement user authentication with OAuth")
    String title;

    @Schema(description = "Description of the task", example = "Implement OAuth-based authentication for users")
    String description;

    @Schema(description = "ID of the project", example = "1")
    Long projectId;

    @Schema(description = "ID of the department", example = "2")
    Long departmentId;

    @Schema(description = "ID of the user who is assigned to the task", example = "4")
    Long assigneeId;

    @Schema(description = "Start date of the task", example = "2023-01-01")
    LocalDate startDate;

    @Schema(description = "Due date of the task", example = "2023-01-15")
    LocalDate dueDate;

    @Schema(description = "Completed date of the task", example = "2023-01-14")
    LocalDate completedDate;

    @Schema(description = "Progress of the task (0-100)", example = "50")
    Integer progress;

    @Schema(description = "Whether the task has files attached", example = "true")
    Boolean hasFiles;

    @Schema(description = "Status of the task", example = "IN_PROGRESS")
    TaskStatus status;

    @Schema(description = "Priority level of the task", example = "HIGH")
    PriorityLevel priority;
}