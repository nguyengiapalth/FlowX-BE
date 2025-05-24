package project.ii.flowx.model.dto.project;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import project.ii.flowx.model.dto.department.DepartmentResponse;
import project.ii.flowx.shared.enums.PriorityLevel;
import project.ii.flowx.shared.enums.ProjectStatus;

import java.time.Instant;
import java.time.LocalDate;

@Schema(description = "Project Response")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectResponse {
    Long id;
    String name;
    String description;
    LocalDate startDate;
    LocalDate endDate;
    DepartmentResponse department;
    Instant createdAt;
    Instant updatedAt;
    ProjectStatus status;
    PriorityLevel priority;
}