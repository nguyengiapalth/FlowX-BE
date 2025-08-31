package project.ii.flowx.module.manage.dto.project;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import project.ii.flowx.applications.enums.PriorityLevel;
import project.ii.flowx.applications.enums.ProjectStatus;

import java.time.LocalDate;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Schema(description = "Project Update Request")
public class ProjectUpdateRequest {
    @Size(min = 2, max = 100, message = "Project name must be between 2 and 100 characters")
    @Schema(description = "Name of the project", example = "FlowX Development")
    String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Schema(description = "Description of the project", example = "A project to develop the FlowX application")
    String description;

    @Schema(description = "Start date of the project", example = "2023-01-01")
    LocalDate startDate;

    @Future(message = "End date must be in the future")
    @Schema(description = "End date of the project", example = "2023-12-31")
    LocalDate endDate;

    @Schema(description = "Status of the project", example = "IN_PROGRESS")
    ProjectStatus status;

    @Schema(description = "Priority level of the project", example = "HIGH")
    PriorityLevel priority;
}