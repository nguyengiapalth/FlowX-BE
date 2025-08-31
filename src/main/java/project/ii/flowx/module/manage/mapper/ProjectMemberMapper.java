package project.ii.flowx.module.manage.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import project.ii.flowx.module.manage.entity.ProjectMember;
import project.ii.flowx.module.manage.dto.projectmember.ProjectMemberCreateRequest;
import project.ii.flowx.module.manage.dto.projectmember.ProjectMemberResponse;

import java.util.List;

/**
 * Mapper interface for converting between ProjectMember entity and ProjectMember DTOs.
 * This interface uses MapStruct to generate the implementation at compile time.
 */
@Mapper(componentModel = "spring")
public interface ProjectMemberMapper {

    /**
     * Convert ProjectMember entity to ProjectMemberResponse DTO
     * Maps all fields directly from entity including userId and project object
     */
    ProjectMemberResponse toProjectMemberResponse(ProjectMember projectMember);

    /**
     * Convert ProjectMemberCreateRequest to ProjectMember entity
     * Ignores auto-generated and system-managed fields
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "project", ignore = true) // Will be set by service
    ProjectMember toProjectMember(ProjectMemberCreateRequest projectMemberCreateRequest);

    /**
     * Convert list of ProjectMember entities to list of ProjectMemberResponse DTOs
     */
    List<ProjectMemberResponse> toProjectMemberResponseList(List<ProjectMember> projectMembers);
}