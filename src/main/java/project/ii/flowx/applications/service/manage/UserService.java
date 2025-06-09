package project.ii.flowx.applications.service.manage;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ii.flowx.applications.events.UserEvent;
import project.ii.flowx.applications.service.helper.EntityLookupService;
import project.ii.flowx.model.entity.Department;
import project.ii.flowx.model.dto.user.UserCreateRequest;
import project.ii.flowx.model.dto.user.UserResponse;
import project.ii.flowx.model.dto.user.UserUpdateRequest;
import project.ii.flowx.model.dto.user.UserAvatarUpdateRequest;
import project.ii.flowx.model.entity.User;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;
import project.ii.flowx.model.mapper.UserMapper;
import project.ii.flowx.model.repository.UserRepository;
import project.ii.flowx.security.UserPrincipal;
import project.ii.flowx.shared.enums.UserStatus;

import java.util.List;
import java.util.UUID;

import static project.ii.flowx.exceptionhandler.FlowXError.USER_ALREADY_EXISTS;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    UserMapper userMapper;
    EntityLookupService entityLookupService;
    ApplicationEventPublisher eventPublisher;
    PasswordEncoder passwordEncoder;

    @Transactional
    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_HR')")
    public UserResponse createUser(UserCreateRequest userCreateRequest) {
        // check if user already exists
        if (userRepository.existsByEmail(userCreateRequest.getEmail()))
            throw new FlowXException(USER_ALREADY_EXISTS, "User with email " + userCreateRequest.getEmail() + " already exists");

        User user = userMapper.toUser(userCreateRequest);
        String randomPassword = UUID.randomUUID().toString();
        log.info("Random password generated for user {}: {}", user.getEmail(), randomPassword);
        user.setPassword(passwordEncoder.encode(randomPassword));

        user = userRepository.save(user);

        // publish event
        UserEvent.UserCreatedEvent userCreatedEvent = new UserEvent.UserCreatedEvent(user.getId(), user.getEmail(), randomPassword, user.getFullName(), user.getPosition());
        eventPublisher.publishEvent(userCreatedEvent);

        return userMapper.toUserResponse(user);
    }

    @Transactional
    @PreAuthorize("#id == authentication.principal.id or hasAnyAuthority('ROLE_MANAGER', 'ROLE_HR')")
    public UserResponse updateUser(Long id, UserUpdateRequest userUpdateRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "User not found"));
        userMapper.updateUserFromRequest(user, userUpdateRequest);
        user = userRepository.save(user);

        return userMapper.toUserResponse(user);
    }

    @Transactional()
    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_HR')")
    public UserResponse updateUserStatus(Long id, UserStatus status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "User not found"));
        user.setStatus(status);
        user = userRepository.save(user);
        return userMapper.toUserResponse(user);
    }

    @Transactional()
    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER')")
    public UserResponse updateUserDepartment(Long id, Long departmentId) {
        User user = userRepository.findUserById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "User not found"));
        long currentDepartmentId = user.getDepartment() != null ? user.getDepartment().getId() : 0;

        if(departmentId == 0L) {
            log.info("Department id is 0, so we will remove the department from user");
            user.setDepartment(null);
        }
        else {
            Department department = entityLookupService.getDepartmentById(departmentId);
            user.setDepartment(department);
        }

        user = userRepository.save(user);
        // publish event
        UserEvent.UserDepartmentChangedEvent userDepartmentUpdatedEvent
                = new UserEvent.UserDepartmentChangedEvent(user.getId(), currentDepartmentId, departmentId );
        eventPublisher.publishEvent(userDepartmentUpdatedEvent);

        return userMapper.toUserResponse(user);
    }

    @Transactional
    @PreAuthorize("#id == authentication.principal.id or hasAnyAuthority('ROLE_MANAGER', 'ROLE_HR')")
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "User not found"));
        userRepository.delete(user);

        // publish event
        UserEvent.UserDeletedEvent userDeletedEvent = new UserEvent.UserDeletedEvent(user.getId());
        eventPublisher.publishEvent(userDeletedEvent);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public UserResponse getMyProfile() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userPrincipal.getId();

        log.info("User id: {}, user name: {}", userId, userPrincipal.getUsername());

        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "User not found"));

        log.info("user found: {}", user);

        return userMapper.toUserResponse(user);
    }

    @Transactional
    @PreAuthorize("isAuthenticated()")
    public UserResponse updateMyProfile(UserUpdateRequest userUpdateRequest) {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userPrincipal.getId();

        log.info("Updating profile for user id: {}, user name: {}", userId, userPrincipal.getUsername());

        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "User not found"));

        userMapper.updateUserFromRequest(user, userUpdateRequest);
        userRepository.save(user);

        log.info("Profile updated for user: {}", user.getId());

        // Get fresh profile data after update
        return getMyProfile();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return userMapper.toUserResponseList(users);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_HR') or @authorize.hasDepartmentRole('MEMBER', #departmentId)") // or my department
    public List<UserResponse> getUsersByDepartment(Long departmentId) {
        List<User> users = userRepository.findByDepartmentId(departmentId);
        return users.isEmpty() ? null : userMapper.toUserResponseList(users);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        return userMapper.toUserResponse(user);
    }

    @Transactional
    @PreAuthorize("#id == authentication.principal.id or hasAnyAuthority('ROLE_MANAGER', 'ROLE_HR')")
    public UserResponse updateUserAvatarAndBackground(Long id, UserAvatarUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "User not found"));
        
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
            log.info("Updated avatar for user {}", id);
        }
        
        if (request.getBackground() != null) {
            user.setBackground(request.getBackground());
            log.info("Updated background for user {}", id);
        }
        
        userRepository.save(user);
        
        // Check if this is current user updating their own profile
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userPrincipal.getId().equals(id)) {
            // Get fresh profile data after update for current user
            return getMyProfile();
        }
        
        // For admin updating other users, return mapped response
        return userMapper.toUserResponse(user);
    }

    @Transactional
    @PreAuthorize("isAuthenticated()")
    public UserResponse updateMyAvatarAndBackground(UserAvatarUpdateRequest request) {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userPrincipal.getId();

        log.info("Updating avatar/background for user id: {}", userId);

        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "User not found"));

        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
            log.info("Updated avatar for current user {}", userId);
        }
        
        if (request.getBackground() != null) {
            user.setBackground(request.getBackground());
            log.info("Updated background for current user {}", userId);
        }

        userRepository.save(user);

        log.info("Avatar/background updated for user: {}", user.getId());

        // Get fresh profile data after update
        return getMyProfile();
    }

    @Transactional
    @PreAuthorize("isAuthenticated()")
    public UserResponse updateMyAvatar(String avatar) {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userPrincipal.getId();

        log.info("Updating avatar for user id: {}", userId);

        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "User not found"));

        user.setAvatar(avatar);
        userRepository.save(user);

        log.info("Avatar updated for user: {}", user.getId());

        // Get fresh profile data after update
        return getMyProfile();
    }

    @Transactional
    @PreAuthorize("isAuthenticated()")
    public UserResponse updateMyBackground(String background) {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userPrincipal.getId();

        log.info("Updating background for user id: {}", userId);

        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "User not found"));

        user.setBackground(background);
        userRepository.save(user);

        log.info("Background updated for user: {}", user.getId());

        // Get fresh profile data after update
        return getMyProfile();
    }
}

