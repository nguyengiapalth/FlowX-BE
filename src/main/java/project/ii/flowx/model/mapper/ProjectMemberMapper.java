package project.ii.flowx.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import project.ii.flowx.model.entity.ProjectMember;
import project.ii.flowx.model.dto.projectmember.ProjectMemberCreateRequest;
import project.ii.flowx.model.dto.projectmember.ProjectMemberResponse;
import project.ii.flowx.model.dto.projectmember.ProjectMemberUpdateRequest;

import java.util.List;

/**
 * Mapper interface for converting between ProjectMember entity and ProjectMember DTOs.
 * This interface uses MapStruct to generate the implementation at compile time.
 */
@Mapper(componentModel = "spring")
public interface ProjectMemberMapper {

    ProjectMemberResponse toProjectMemberResponse(ProjectMember projectMember);

    @Mapping(target = "project.id", source = "projectId")
    @Mapping(target = "user.id", source = "userId")
    ProjectMember toProjectMember(ProjectMemberCreateRequest projectMemberCreateRequest);

    List<ProjectMemberResponse> toProjectMemberResponseList(List<ProjectMember> projectMembers);
}