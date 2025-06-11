package project.ii.flowx.model.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import project.ii.flowx.model.dto.department.DepartmentResponse;
import project.ii.flowx.model.dto.file.FileResponse;
import project.ii.flowx.model.dto.project.ProjectResponse;
import project.ii.flowx.model.dto.user.UserResponse;
import project.ii.flowx.shared.enums.ContentTargetType;
import project.ii.flowx.shared.enums.PriorityLevel;
import project.ii.flowx.shared.enums.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Task Response")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskResponse {
    Long id;
    String title;
    String description;
    ContentTargetType targetType;
    Long targetId;
    UserResponse assigner;
    UserResponse assignee;
    LocalDate startDate;
    LocalDate dueDate;
    LocalDate completedDate;
    Integer progress;
    Boolean hasFiles;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    TaskStatus status;
    PriorityLevel priority;
    
    @Schema(description = "List of attached files")
    List<FileResponse> files;
}