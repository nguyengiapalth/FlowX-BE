package project.ii.flowx.module.manage.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ii.flowx.applications.events.ProjectEvent;
import project.ii.flowx.applications.helper.EntityLookupService;
import project.ii.flowx.module.manage.dto.project.ProjectResponse;
import project.ii.flowx.module.manage.entity.Project;
import project.ii.flowx.module.manage.entity.ProjectMember;
import project.ii.flowx.module.manage.ProjectMemberRepository;
import project.ii.flowx.module.user.repository.UserRepository;
import project.ii.flowx.module.manage.dto.projectmember.ProjectMemberCreateRequest;
import project.ii.flowx.module.manage.dto.projectmember.ProjectMemberResponse;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;
import project.ii.flowx.module.manage.mapper.ProjectMemberMapper;
import project.ii.flowx.applications.enums.RoleDefault;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectMemberService {
    ProjectMemberRepository projectMemberRepository;
    ProjectMemberMapper projectMemberMapper;
    EntityLookupService entityLookupService;
    ProjectService projectService;
    ApplicationEventPublisher eventPublisher;
    UserRepository userRepository;

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or @authorize.hasProjectRole('MANAGER', #projectMemberCreateRequest.getProjectId())")
    public ProjectMemberResponse createProjectMember(ProjectMemberCreateRequest projectMemberCreateRequest) {
        // Validate project exists
        ProjectResponse projectResponse = projectService.getProjectById(projectMemberCreateRequest.getProjectId());
        
        if (!userRepository.existsById(projectMemberCreateRequest.getUserId())) {
            throw new FlowXException(FlowXError.NOT_FOUND, "User not found with ID: " + projectMemberCreateRequest.getUserId());
        }

        // Check if user is already a member
        boolean memberExists = projectMemberRepository.existsByProjectIdAndUserId(
            projectMemberCreateRequest.getProjectId(),
            projectMemberCreateRequest.getUserId()
        );

        if (memberExists) {
            throw new FlowXException(FlowXError.ALREADY_EXISTS, "User is already a member of this project");
        }
        
        // Create project member using mapper
        ProjectMember projectMember = projectMemberMapper.toProjectMember(projectMemberCreateRequest);
        projectMember = projectMemberRepository.save(projectMember);

        // Publish add member event
        ProjectEvent.AddMemberEvent event = new ProjectEvent.AddMemberEvent(
                projectMemberCreateRequest.getProjectId(),
                projectMemberCreateRequest.getUserId(),
                projectMember.getRole()
        );
        eventPublisher.publishEvent(event);
        
        log.info("Added user {} to project {} with role {}",
                projectMemberCreateRequest.getUserId(),
                projectMemberCreateRequest.getProjectId(),
                projectMember.getRole()
        );

        return projectMemberMapper.toProjectMemberResponse(projectMember);
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public ProjectMemberResponse updateMemberRole(UUID id, RoleDefault role) {
        ProjectMember projectMember = projectMemberRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Project member not found with ID: " + id));
        
        // Validate role
        if (role == null || role == RoleDefault.USER) {
            throw new FlowXException(FlowXError.BAD_REQUEST,
                    "Invalid role. Only MANAGER or MEMBER roles are allowed for project members.");
        }

        RoleDefault oldRole = projectMember.getRole();
        projectMember.setRole(role);
        projectMember = projectMemberRepository.save(projectMember);
        
        // Publish role change event using userId directly
        ProjectEvent.UpdateMemberRoleEvent event = new ProjectEvent.UpdateMemberRoleEvent(
                projectMember.getProject().getId(),
                projectMember.getUserId(), // Use userId instead of user.getId()
                role
        );
        eventPublisher.publishEvent(event);
        
        log.info("Updated project member {} role from {} to {} in project {}",
                projectMember.getUserId(),
                oldRole,
                role,
                projectMember.getProject().getId()
        );
        
        return projectMemberMapper.toProjectMemberResponse(projectMember);
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or @authorize.hasProjectRole('MANAGER', #projectId)")
    public void deleteProjectMember(UUID id) {
        ProjectMember projectMember = projectMemberRepository.findById(id)
                .orElse(null);
        
        if (projectMember == null) {
            throw new FlowXException(FlowXError.NOT_FOUND, "Project member not found with ID: " + id);
        }
        
        UUID projectId = projectMember.getProject().getId();
        UUID userId = projectMember.getUserId();
        
        projectMemberRepository.deleteById(id);
        
        // Publish member removal event
        ProjectEvent.RemoveMemberEvent event = new ProjectEvent.RemoveMemberEvent(projectId, userId);
        eventPublisher.publishEvent(event);
        
        log.info("Removed user {} from project {} with role {}", userId, projectId, projectMember.getRole());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or @authorize.hasProjectRole('MEMBER', #id)")
    public ProjectMemberResponse getProjectMemberById(UUID id) {
        ProjectMember projectMember = projectMemberRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Project member not found with ID: " + id));
        return projectMemberMapper.toProjectMemberResponse(projectMember);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or @authorize.hasProjectRole('MEMBER', #projectId)")
    public List<ProjectMemberResponse> getByProject(UUID projectId) {
        // Validate project exists
        ProjectResponse projectResponse = projectService.getProjectById(projectId);
        
        List<ProjectMember> members = projectMemberRepository.findByProjectId(projectId);
        if (members.isEmpty()) return List.of();

        return projectMemberMapper.toProjectMemberResponseList(members);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<ProjectMemberResponse> getByUserId(UUID userId) {
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new FlowXException(FlowXError.NOT_FOUND, "User not found with ID: " + userId);
        }
        
        List<ProjectMember> members = projectMemberRepository.findByUserId(userId);
        return projectMemberMapper.toProjectMemberResponseList(members);
    }
}