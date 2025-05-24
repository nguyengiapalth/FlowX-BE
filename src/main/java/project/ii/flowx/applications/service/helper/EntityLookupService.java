package project.ii.flowx.applications.service.helper;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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

    public Content getContentById(Long id) {
        return contentRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Content not found"));
    }

    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Department not found"));
    }

    public File getFileById(Long id) {
        return fileRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "File not found"));
    }

    public InvalidToken getInvalidTokenById(Long id) {
        return invalidTokenRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Invalid token not found"));
    }

    public Notification getNotificationById(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Notification not found"));
    }

    public ProjectMember getProjectMemberById(Long id) {
        return projectMemberRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Project member not found"));
    }

    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Project not found"));
    }

    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Task not found"));
    }

    public UserActivityLog getUserActivityLogById(Long id) {
        return userActivityLogRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "User activity log not found"));
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.USER_NOT_FOUND, "User not found"));
    }

    public UserRole getUserRoleById(Long id) {
        return userRoleRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "User role not found"));
    }

    public Role getRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Role not found"));
    }

    public <T> T getEntityById(JpaRepository<T, Long> repository, Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Entity not found"));
    }

    public Optional<Role> getRoleByName(String roleName) {
        return roleRepository.findByName(roleName);
    }
}
