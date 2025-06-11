package project.ii.flowx.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import project.ii.flowx.applications.service.helper.MinioService;
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
public abstract class ProjectMapper {
    @Autowired
    protected MinioService minioService;

    @Mapping(target = "background", source = "background", qualifiedByName = "objectKeyToUrl")
    public abstract ProjectResponse toProjectResponse(Project project);

    public abstract Project toProject(ProjectCreateRequest projectCreateRequest);

    public abstract void updateProjectFromRequest(@MappingTarget Project project, ProjectUpdateRequest projectUpdateRequest);

    public abstract List<ProjectResponse> toProjectResponseList(List<Project> projects);

    @Named("objectKeyToUrl")
    protected String objectKeyToUrl(String objectKey) {
        if (objectKey == null || objectKey.trim().isEmpty()) return null;

        try {
            // 24 hours expiry
            return minioService.getPresignedDownloadUrlFromObjectKey(objectKey, 3600 * 24);
        } catch (Exception e) {
            return null;
        }
    }

}