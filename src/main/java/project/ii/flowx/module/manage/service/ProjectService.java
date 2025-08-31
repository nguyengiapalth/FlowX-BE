package project.ii.flowx.module.manage.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ii.flowx.module.auth.service.AuthorizationService;
import project.ii.flowx.applications.helper.EntityLookupService;

import project.ii.flowx.module.manage.entity.Project;
import project.ii.flowx.module.manage.ProjectRepository;
import project.ii.flowx.module.manage.dto.project.ProjectCreateRequest;
import project.ii.flowx.module.manage.dto.project.ProjectResponse;
import project.ii.flowx.module.manage.dto.project.ProjectUpdateRequest;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;
import project.ii.flowx.module.manage.mapper.ProjectMapper;
import project.ii.flowx.security.UserPrincipal;
import project.ii.flowx.applications.enums.ProjectStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectService {
    ProjectRepository projectRepository;
    AuthorizationService authorizationService;
    ProjectMapper projectMapper;
    EntityLookupService entityLookupService;

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public ProjectResponse createProject(ProjectCreateRequest projectCreateRequest) {
        Project project = projectMapper.toProject(projectCreateRequest);
        
        // Set default values if not provided
        if (project.getStatus() == null) {
            project.setStatus(ProjectStatus.NOT_STARTED);
        }
        
        // If project is set to IN_PROGRESS, set start date
        if (project.getStatus() == ProjectStatus.IN_PROGRESS && project.getStartDate() == null) {
            project.setStartDate(LocalDate.now());
        }

        project = projectRepository.save(project);
        return projectMapper.toProjectResponse(project);
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or @authorize.hasProjectRole('MANAGER', #id)")
    public ProjectResponse updateProject(UUID id, ProjectUpdateRequest projectUpdateRequest) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Project not found"));
        
        projectMapper.updateProjectFromRequest(project, projectUpdateRequest);
        project = projectRepository.save(project);
        
        return projectMapper.toProjectResponse(project);
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or @authorize.hasProjectRole('MANAGER', #id)")
    public ProjectResponse updateProjectStatus(UUID id, ProjectStatus status) {
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
    public ProjectResponse updateProjectBackground(UUID id, String background) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Project not found"));

        project.setBackground(background);
        project = projectRepository.save(project);
        return projectMapper.toProjectResponse(project);
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or @authorize.hasProjectRole('MANAGER', #id)")
    public void deleteProject(UUID id) {
        projectRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Project not found"));
        projectRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public ProjectResponse getProjectById(UUID id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Project not found"));
        return projectMapper.toProjectResponse(project);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<ProjectResponse> getAllProjects() {
        List<Project> projects = projectRepository.findAll();
        // Filter projects based on user's access rights
        List<ProjectResponse> projectResponses = projectMapper.toProjectResponseList(projects);
        return filterAccessibleProjects(projectResponses);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<ProjectResponse> getMyProjects() {
        UUID userId = getUserId();
        List<Project> projects = projectRepository.findByMemberId(userId);
        if (projects.isEmpty()) {return List.of();}
        log.info("Found {} projects for user {}", projects, userId);
        return projectMapper.toProjectResponseList(projects);
    }

    // helper method to get the authenticated user's ID
    private UUID getUserId() {
        var context = SecurityContextHolder.getContext();
        if (context.getAuthentication() == null || context.getAuthentication().getPrincipal() == null)
            throw new FlowXException(FlowXError.UNAUTHENTICATED, "No authenticated user found");

        UserPrincipal userPrincipal = (UserPrincipal) context.getAuthentication().getPrincipal();
        return userPrincipal.getId();
    }

    private List<ProjectResponse> filterAccessibleProjects(List<ProjectResponse> projects) {
        return projects.stream()
                .filter(project -> authorizationService.hasProjectRole("MEMBER", project.getId()))
                .toList();
    }
}