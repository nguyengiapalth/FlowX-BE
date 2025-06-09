package project.ii.flowx.applications.service.helper;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import project.ii.flowx.model.entity.*;
import project.ii.flowx.model.repository.*;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EntityLookupService {
    ContentRepository contentRepository;
    DepartmentRepository departmentRepository;
    FileRepository fileRepository;
    InvalidTokenRepository invalidTokenRepository;
    NotificationRepository notificationRepository;
    ProjectMemberRepository projectMemberRepository;
    ProjectRepository projectRepository;
    TaskRepository taskRepository;
    UserActivityLogRepository userActivityLogRepository;
    UserRepository userRepository;
    UserRoleRepository userRoleRepository;
    RoleRepository roleRepository;

    @Cacheable(value = "contentById", key = "#id")
    public Content getContentById(Long id) {
        return contentRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Content not found"));
    }

    @Cacheable(value = "departmentById", key = "#id")
    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Department not found"));
    }

    @Cacheable(value = "fileById", key = "#id")
    public File getFileById(Long id) {
        return fileRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "File not found"));
    }

    public InvalidToken getInvalidTokenById(Long id) {
        return invalidTokenRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Invalid token not found"));
    }

    @Cacheable(value = "notificationById", key = "#id")
    public Notification getNotificationById(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Notification not found"));
    }

    public ProjectMember getProjectMemberById(Long id) {
        return projectMemberRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Project member not found"));
    }

    @Cacheable(value = "projectById", key = "#id")
    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Project not found"));
    }

    @Cacheable(value = "taskById", key = "#id")
    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Task not found"));
    }

    public UserActivityLog getUserActivityLogById(Long id) {
        return userActivityLogRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "User activity log not found"));
    }

    @Cacheable(value = "userById", key = "#id")
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.USER_NOT_FOUND, "User not found"));
    }

    public UserRole getUserRoleById(Long id) {
        return userRoleRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "User role not found"));
    }

    @Cacheable(value = "roleById", key = "#id")
    public Role getRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Role not found"));
    }

    @Cacheable(value = "roleByName", key = "#roleName")
    public Optional<Role> getRoleByName(String roleName) {
        return roleRepository.findByName(roleName);
    }
}
