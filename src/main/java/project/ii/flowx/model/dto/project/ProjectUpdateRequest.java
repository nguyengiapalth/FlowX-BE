package project.ii.flowx.model.dto.project;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import project.ii.flowx.shared.enums.PriorityLevel;
import project.ii.flowx.shared.enums.ProjectStatus;

import java.time.LocalDate;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Schema(description = "Project Update Request")
public class ProjectUpdateRequest {
    @Schema(description = "Name of the project", example = "FlowX Development")
    String name;

    @Schema(description = "Description of the project", example = "A project to develop the FlowX application")
    String description;

    @Schema(description = "Start date of the project", example = "2023-01-01")
    LocalDate startDate;

    @Schema(description = "End date of the project", example = "2023-12-31")
    LocalDate endDate;

    @Schema(description = "Status of the project", example = "IN_PROGRESS")
    ProjectStatus status;

    @Schema(description = "Priority level of the project", example = "HIGH")
    PriorityLevel priority;
}