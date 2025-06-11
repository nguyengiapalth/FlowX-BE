package project.ii.flowx.model.dto.project;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import project.ii.flowx.model.entity.User;
import project.ii.flowx.shared.enums.PriorityLevel;
import project.ii.flowx.shared.enums.ProjectStatus;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Project Create Request")
@Data
@Getter
@Setter
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ProjectCreateRequest {
    @Schema(description = "Name of the project", example = "FlowX Development")
    String name;

    @Schema(description = "Description of the project", example = "A project to develop the FlowX application")
    String description;

    @Schema(description = "Background image URL of the project", example = "https://example.com/project-bg.jpg")
    String background;

    @Schema(description = "Start date of the project", example = "2023-01-01")
    LocalDate startDate;

    @Schema(description = "End date of the project", example = "2023-12-31")
    LocalDate endDate;

    @Schema(description = "ID of the department", example = "2")
    Long departmentId;

    @Schema(description = "ID of the user who created the project", example = "1")
    Long createdById;

    @Schema(description = "Status of the project", example = "IN_PROGRESS")
    ProjectStatus status;

    @Schema(description = "Priority level of the project", example = "HIGH")
    PriorityLevel priority;

    @Schema(description = "List of user IDs to be added as members to the project")
    List<Long> memberIds; // List of user IDs to be added as members to the project
}
