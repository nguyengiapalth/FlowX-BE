package project.ii.flowx.model.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import project.ii.flowx.shared.enums.ContentTargetType;
import project.ii.flowx.shared.enums.PriorityLevel;
import project.ii.flowx.shared.enums.TaskStatus;

import java.time.LocalDate;

@Schema(description = "Task Create Request")
@Data
@Getter
@Setter
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class TaskCreateRequest {
    @Schema(description = "Title of the task", example = "Implement user authentication")
    String title;

    @Schema(description = "Description of the task", example = "Implement JWT-based authentication for users")
    String description;

    @Enumerated(EnumType.STRING)
    @Schema(description = "Type of the entity involved in the task", example = "DEPARTMENT")
    ContentTargetType targetType;

    @Schema(description = "ID of the target entity for the task", example = "1")
    Long targetId;

    @Schema(description = "ID of the user who assigned the task", example = "3")
    Long assignerId;

    @Schema(description = "ID of the user who is assigned to the task", example = "4")
    Long assigneeId;

    @Schema(description = "Start date of the task", example = "2023-01-01")
    LocalDate startDate;

    @Schema(description = "Due date of the task", example = "2023-01-15")
    LocalDate dueDate;

    @Schema(description = "Progress of the task (0-100)", example = "0")
    Integer progress;

    @Schema(description = "Whether the task has files attached", example = "false")
    Boolean hasFiles;

    @Schema(description = "Status of the task", example = "TO_DO")
    TaskStatus status;

    @Schema(description = "Priority level of the task", example = "MEDIUM")
    PriorityLevel priority;
}
