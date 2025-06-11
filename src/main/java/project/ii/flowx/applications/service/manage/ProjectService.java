package project.ii.flowx.applications.service.manage;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ii.flowx.applications.service.helper.EntityLookupService;
import project.ii.flowx.model.entity.Department;
import project.ii.flowx.model.entity.Project;
import project.ii.flowx.model.entity.ProjectMember;
import project.ii.flowx.model.repository.ProjectRepository;
import project.ii.flowx.model.repository.ProjectMemberRepository;
import project.ii.flowx.model.dto.project.ProjectCreateRequest;
import project.ii.flowx.model.dto.project.ProjectResponse;
import project.ii.flowx.model.dto.project.ProjectUpdateRequest;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;
import project.ii.flowx.model.mapper.ProjectMapper;
import project.ii.flowx.security.UserPrincipal;
import project.ii.flowx.shared.enums.ProjectStatus;
import project.ii.flowx.shared.enums.RoleDefault;
import project.ii.flowx.shared.enums.MemberStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectService {
    ProjectRepository projectRepository;
    ProjectMemberRepository projectMemberRepository;
    ProjectMapper projectMapper;
    EntityLookupService entityLookupService;

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_MANAGER') " +
            "or @authorize.hasDepartmentRole('MANAGER', #projectCreateRequest.getDepartmentId())")
    public ProjectResponse createProject(ProjectCreateRequest projectCreateRequest) {
        // Validate department ID
        Department department = entityLookupService.getDepartmentById(projectCreateRequest.getDepartmentId());
        Project project = projectMapper.toProject(projectCreateRequest);
        project.setDepartment(department);
        
        // Set default values if not provided
        if (project.getStatus() == null) {
            project.setStatus(ProjectStatus.NOT_STARTED);
        }
        
        // If project is set to IN_PROGRESS, set start date
        if (project.getStatus() == ProjectStatus.IN_PROGRESS && project.getStartDate() == null) {
            project.setStartDate(LocalDate.now());
        }

        project = projectRepository.save(project);
        
        // Add members to the project if memberIds is provided
        if (projectCreateRequest.getMemberIds() != null && !projectCreateRequest.getMemberIds().isEmpty()) {
            log.info("Adding {} members to project {}", projectCreateRequest.getMemberIds().size(), project.getId());
            addMembersToProject(project.getId(), projectCreateRequest.getMemberIds());
        }
        
        return projectMapper.toProjectResponse(project);
    }

    // Helper method to add members to project
    @Transactional
    @PreAuthorize("hasAuthority('ROLE_MANAGER') " +
            "or @authorize.hasProjectRole('MANAGER', #projectId)")
    protected void addMembersToProject(Long projectId, List<Long> memberIds) {
        List<ProjectMember> membersToAdd = new ArrayList<>();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Project not found"));
        
        for (Long userId : memberIds) {
            // Validate user exists and get user entity
            var user = entityLookupService.getUserById(userId);
            
            // Check if user is already a member of this project
            boolean memberExists = projectMemberRepository.existsByProjectIdAndUserId(projectId, userId);
            if (!memberExists) {
                ProjectMember projectMember = ProjectMember.builder()
                    .project(project)
                    .user(user)
                    .role(RoleDefault.MEMBER) // Default role
                    .status(MemberStatus.ACTIVE)
                    .joinDate(LocalDate.now())
                    .build();
                membersToAdd.add(projectMember);
            } else {
                log.warn("User {} is already a member of project {}", userId, projectId);
            }
        }
        
        if (!membersToAdd.isEmpty()) {
            projectMemberRepository.saveAll(membersToAdd);
            log.info("Successfully added {} members to project {}", membersToAdd.size(), projectId);
        }
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or @authorize.hasProjectRole('MANAGER', #id)")
    public ProjectResponse updateProject(Long id, ProjectUpdateRequest projectUpdateRequest) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Project not found"));
        
        projectMapper.updateProjectFromRequest(project, projectUpdateRequest);
        project = projectRepository.save(project);
        
        return projectMapper.toProjectResponse(project);
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or @authorize.hasProjectRole('MANAGER', #id)")
    public ProjectResponse updateProjectStatus(Long id, ProjectStatus status) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Project not found"));
        project.setStatus(status);

        // If project is in progress, set start date if not already set
        if (status == ProjectStatus.IN_PROGRESS && project.getStartDate() == null)
            project.setStartDate(LocalDate.now());

        // If project is completed, set end date if not already set
        if (status == ProjectStatus.COMPLETED && project.getEndDate() == null)
            project.setEndDate(LocalDate.now());

        project = projectRepository.save(project);
        return projectMapper.toProjectResponse(project);
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or @authorize.hasProjectRole('MANAGER', #id)")
    public ProjectResponse completeProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Project not found"));

        project.setStatus(ProjectStatus.COMPLETED);
        project.setEndDate(LocalDate.now());

        project = projectRepository.save(project);
        return projectMapper.toProjectResponse(project);
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or @authorize.hasProjectRole('MANAGER', #id)")
    public ProjectResponse updateProjectBackground(Long id, String background) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Project not found"));

        project.setBackground(background);
        project = projectRepository.save(project);
        return projectMapper.toProjectResponse(project);
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or @authorize.hasProjectRole('MANAGER', #id)")
    public void deleteProject(Long id) {
        projectRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Project not found"));
        projectRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or @authorize.hasProjectRole('MEMBER', #id)")
    public ProjectResponse getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Project not found"));
        return projectMapper.toProjectResponse(project);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public List<ProjectResponse> getAllProjects() {
        List<Project> projects = projectRepository.findAll();
        return projectMapper.toProjectResponseList(projects);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER') or @authorize.hasDepartmentRole('MEMBER', #departmentId)")
    public List<ProjectResponse> getProjectsByDepartmentId(long departmentId) {
        List<Project> projects = projectRepository.findByDepartmentId(departmentId);
        return projectMapper.toProjectResponseList(projects);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public List<ProjectResponse> getProjectsByStatus(ProjectStatus status) {
        List<Project> projects = projectRepository.findByStatus(status);
        return projectMapper.toProjectResponseList(projects);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<ProjectResponse> getMyProjects() {
        Long userId = getUserId();
        List<Project> projects = projectRepository.findByMemberId(userId);
        if (projects.isEmpty()) {return List.of();}
        return projectMapper.toProjectResponseList(projects);
    }

    private Long getUserId() {
        var context = SecurityContextHolder.getContext();
        if (context.getAuthentication() == null || context.getAuthentication().getPrincipal() == null)
            throw new FlowXException(FlowXError.UNAUTHORIZED, "No authenticated user found");

        UserPrincipal userPrincipal = (UserPrincipal) context.getAuthentication().getPrincipal();
        return userPrincipal.getId();
    }
}