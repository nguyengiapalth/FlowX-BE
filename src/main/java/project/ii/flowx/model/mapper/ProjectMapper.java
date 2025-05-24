package project.ii.flowx.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import project.ii.flowx.model.entity.Project;
import project.ii.flowx.model.dto.project.ProjectCreateRequest;
import project.ii.flowx.model.dto.project.ProjectResponse;
import project.ii.flowx.model.dto.project.ProjectUpdateRequest;

import java.util.List;

/**
 * Mapper interface for converting between Project entity and Project DTOs.
 * This interface uses MapStruct to generate the implementation at compile time.
 */
@Mapper(componentModel = "spring")
public interface ProjectMapper {

    ProjectResponse toProjectResponse(Project project);

    @Mapping(target = "department.id", source = "departmentId")
    Project toProject(ProjectCreateRequest projectCreateRequest);

    void updateProjectFromRequest(@MappingTarget Project project, ProjectUpdateRequest projectUpdateRequest);

    List<ProjectResponse> toProjectResponseList(List<Project> projects);
}