package project.ii.flowx.applications.service.manage;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ii.flowx.applications.events.ProjectEvent;
import project.ii.flowx.applications.service.helper.EntityLookupService;
import project.ii.flowx.model.entity.Project;
import project.ii.flowx.model.entity.ProjectMember;
import project.ii.flowx.model.repository.ProjectMemberRepository;
import project.ii.flowx.model.dto.projectmember.ProjectMemberCreateRequest;
import project.ii.flowx.model.dto.projectmember.ProjectMemberResponse;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;
import project.ii.flowx.model.mapper.ProjectMemberMapper;
import project.ii.flowx.shared.enums.MemberStatus;
import project.ii.flowx.shared.enums.RoleDefault;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectMemberService {
    ProjectMemberRepository projectMemberRepository;
    ProjectMemberMapper projectMemberMapper;
    EntityLookupService entityLookupService;
    ApplicationEventPublisher eventPublisher;

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or @authorize.hasProjectRole('MANAGER', #projectMemberCreateRequest.getProjectId())")
    public ProjectMemberResponse createProjectMember(ProjectMemberCreateRequest projectMemberCreateRequest) {
        Project project = entityLookupService.getProjectById(projectMemberCreateRequest.getProjectId());

        boolean memberExists = projectMemberRepository.existsByProjectIdAndUserId(
            projectMemberCreateRequest.getProjectId(),
            projectMemberCreateRequest.getUserId()
        );

        if (memberExists) throw new FlowXException(FlowXError.ALREADY_EXISTS, "Người dùng đã là thành viên của project này");
        ProjectMember projectMember = projectMemberMapper.toProjectMember(projectMemberCreateRequest);
        if (projectMember.getStatus() == null) projectMember.setStatus(MemberStatus.ACTIVE);
        if (projectMember.getJoinDate() == null) projectMember.setJoinDate(LocalDate.now());


        projectMember = projectMemberRepository.save(projectMember);

        ProjectEvent.AddMemberEvent event = new ProjectEvent.AddMemberEvent(
                projectMemberCreateRequest.getProjectId(),
                projectMemberCreateRequest.getUserId(),
                projectMember.getRole()
        );
        eventPublisher.publishEvent(event);

        return projectMemberMapper.toProjectMemberResponse(projectMember);
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public ProjectMemberResponse updateMemberRole(Long id, RoleDefault role) {
        ProjectMember projectMember = projectMemberRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Không tìm thấy project member với ID: " + id));
        // Kiểm tra xem role có hợp lệ không
        if (role == null || role == RoleDefault.USER || role == RoleDefault.HR)
            throw new FlowXException(FlowXError.BAD_REQUEST,
                    "Role không hợp lệ. Chỉ có thể cập nhật thành viên với role MANAGER hoặc MEMBER.");

        projectMember.setRole(role);
        projectMember = projectMemberRepository.save(projectMember);
        // Publish event for role change
        ProjectEvent.UpdateMemberRoleEvent event = new ProjectEvent.UpdateMemberRoleEvent(
                projectMember.getProject().getId(),
                projectMember.getUser().getId(),
                role
        );
        eventPublisher.publishEvent(event);
        return projectMemberMapper.toProjectMemberResponse(projectMember);
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or @authorize.hasProjectRole('MANAGER', #projectMemberCreateRequest.getProjectId())")
    public ProjectMemberResponse updateMemberStatus(Long id, MemberStatus status) {
        ProjectMember projectMember = projectMemberRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Không tìm thấy project member với ID: " + id));
        // Validate status transition nếu cần
        validateStatusTransition(projectMember.getStatus(), status);

        projectMember.setStatus(status);
        projectMember = projectMemberRepository.save(projectMember);

        return projectMemberMapper.toProjectMemberResponse(projectMember);
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or @authorize.hasProjectRole('MANAGER', #projectMemberCreateRequest.getProjectId())")
    public void bulkUpdateMemberStatus(List<Long> memberIds, MemberStatus status) {

        List<ProjectMember> members = projectMemberRepository.findAllById(memberIds);

        if (members.size() != memberIds.size()) {
            throw new FlowXException(FlowXError.FORBIDDEN, "Không tìm thấy một hoặc nhiều project member với ID trong danh sách");
        }

        members.forEach(member -> {
            validateStatusTransition(member.getStatus(), status);
            member.setStatus(status);
        });

        projectMemberRepository.saveAll(members);
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or @authorize.hasProjectRole('MANAGER', #projectMemberCreateRequest.getProjectId())")
    public void deleteProjectMember(Long id) {
        if (!projectMemberRepository.existsById(id))
            throw new FlowXException(FlowXError.NOT_FOUND, "Không tìm thấy project member với ID: " + id);
        projectMemberRepository.deleteById(id);
        // Publish event for member removal
//        ProjectEvent.RemoveMemberEvent event = new ProjectEvent.RemoveMemberEvent()
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public ProjectMemberResponse getProjectMemberById(Long id) {
        ProjectMember projectMember = projectMemberRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Không tìm thấy project member với ID: " + id));
        return projectMemberMapper.toProjectMemberResponse(projectMember);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or @authorize.hasProjectRole('MEMBER', #projectId)")
    public List<ProjectMemberResponse> getByProject(Long projectId) {
        List<ProjectMember> members = projectMemberRepository.findByProjectId(projectId);
        if (members.isEmpty()) {return List.of();}
        return projectMemberMapper.toProjectMemberResponseList(members);
    }

    // Helper methods
    private void validateStatusTransition(MemberStatus currentStatus, MemberStatus newStatus) {
        // Implement business logic for valid status transitions
        if (currentStatus == MemberStatus.INACTIVE && newStatus == MemberStatus.ACTIVE) {}
        // TODO:
    }
}