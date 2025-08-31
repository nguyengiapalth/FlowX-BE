package project.ii.flowx.module.manage.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import project.ii.flowx.module.file.dto.FileResponse;
import project.ii.flowx.applications.enums.Visibility;
import project.ii.flowx.applications.enums.PriorityLevel;
import project.ii.flowx.applications.enums.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Task Response")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskResponse {
    UUID id;
    String title;
    String description;
    Visibility targetType;
    UUID targetId;
    
    @Schema(description = "ID of the user who assigned this task")
    UUID assignerId;
    
    @Schema(description = "ID of the user assigned to this task")
    UUID assigneeId;
    
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