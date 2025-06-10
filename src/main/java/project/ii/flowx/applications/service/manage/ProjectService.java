package project.ii.flowx.applications.service.manage;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ii.flowx.applications.service.helper.EntityLookupService;
import project.ii.flowx.model.entity.Department;
import project.ii.flowx.model.entity.Project;
import project.ii.flowx.model.repository.ProjectRepository;
import project.ii.flowx.model.dto.project.ProjectCreateRequest;
import project.ii.flowx.model.dto.project.ProjectResponse;
import project.ii.flowx.model.dto.project.ProjectUpdateRequest;
import project.ii.flowx.model.dto.project.ProjectBackgroundUpdateRequest;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;
import project.ii.flowx.model.mapper.ProjectMapper;
import project.ii.flowx.shared.enums.ProjectStatus;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectService {
    ProjectRepository projectRepository;
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
        log.info("Creating new project for department ID: {}", department.getId());
        
        // Set default values if not provided
        if (project.getStatus() == null) project.setStatus(ProjectStatus.NOT_STARTED);
        if (project.getStatus() == ProjectStatus.IN_PROGRESS) project.setStartDate(LocalDate.now());


        project = projectRepository.save(project);
        log.info("Created new project with ID: {}", project.getId());
        return projectMapper.toProjectResponse(project);
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
    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER') or @authorize.hasDepartmentRole('MANAGER', #departmentId)")
    public List<ProjectResponse> getProjectsByDepartmentId(long departmentId) {
        List<Project> projects = projectRepository.findByDepartmentId(departmentId);

        log.info("Projects found for department ID {} : {}", departmentId, projects);
        return projectMapper.toProjectResponseList(projects);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public List<ProjectResponse> getProjectsByStatus(ProjectStatus status) {
        List<Project> projects = projectRepository.findByStatus(status);
        log.info("Projects found with status {} : {}", status, projects);
        return projectMapper.toProjectResponseList(projects);
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or @authorize.hasProjectRole('MANAGER', #id)")
    public ProjectResponse updateProjectBackground(Long id, ProjectBackgroundUpdateRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Project not found"));
        
        if (request.getBackground() != null) {
            project.setBackground(request.getBackground());
            log.info("Updated background for project {}", id);
        }
        
        project = projectRepository.save(project);
        return projectMapper.toProjectResponse(project);
    }
}