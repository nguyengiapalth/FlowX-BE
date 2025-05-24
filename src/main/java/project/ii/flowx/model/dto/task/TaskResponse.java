package project.ii.flowx.model.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import project.ii.flowx.model.dto.department.DepartmentResponse;
import project.ii.flowx.model.dto.project.ProjectResponse;
import project.ii.flowx.model.dto.user.UserResponse;
import project.ii.flowx.shared.enums.PriorityLevel;
import project.ii.flowx.shared.enums.TaskStatus;

import java.time.Instant;
import java.time.LocalDate;

@Schema(description = "Task Response")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskResponse {
    Long id;
    String title;
    String description;
    ProjectResponse project;
    DepartmentResponse department;
    UserResponse assigner;
    UserResponse assignee;
    LocalDate startDate;
    LocalDate dueDate;
    LocalDate completedDate;
    Integer progress;
    Instant createdAt;
    Boolean hasFiles;
    Instant updatedAt;
    TaskStatus status;
    PriorityLevel priority;
}