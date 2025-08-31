package project.ii.flowx.applications.helper;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import project.ii.flowx.module.auth.repository.RoleRepository;
import project.ii.flowx.module.auth.repository.UserRoleRepository;
import project.ii.flowx.module.file.FileRepository;
import project.ii.flowx.module.manage.ProjectMemberRepository;
import project.ii.flowx.module.manage.ProjectRepository;
import project.ii.flowx.module.manage.TaskRepository;
import project.ii.flowx.module.user.repository.UserRepository;
import project.ii.flowx.module.content.repository.PostRepository;
import project.ii.flowx.module.content.repository.CommentRepository;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;
import project.ii.flowx.module.manage.entity.Task;
import project.ii.flowx.module.content.entity.Comment;
import project.ii.flowx.module.content.entity.Post;
import project.ii.flowx.module.file.entity.File;
import project.ii.flowx.module.auth.entity.Role;
import project.ii.flowx.module.manage.entity.Project;
import project.ii.flowx.module.manage.entity.ProjectMember;
import project.ii.flowx.module.user.entity.User;
import project.ii.flowx.module.auth.entity.UserRole;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EntityLookupService {
    PostRepository postRepository;
    CommentRepository commentRepository;
    TaskRepository taskRepository;
    UserRepository userRepository;

    public Post getPostById(UUID id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Post not found"));
    }

    public Comment getCommentById(UUID id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Comment not found"));
    }

    public Task getTaskById(UUID id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Task not found"));
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.USER_NOT_FOUND, "User not found"));
    }
}
