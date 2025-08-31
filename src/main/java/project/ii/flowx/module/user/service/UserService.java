package project.ii.flowx.module.user.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ii.flowx.applications.events.UserEvent;
import project.ii.flowx.applications.helper.EntityLookupService;

import project.ii.flowx.module.user.entity.User;
import project.ii.flowx.module.user.dto.UserCreateRequest;
import project.ii.flowx.module.user.dto.UserResponse;
import project.ii.flowx.module.user.dto.UserUpdateRequest;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;
import project.ii.flowx.module.user.mapper.UserMapper;
import project.ii.flowx.module.user.repository.UserRepository;
import project.ii.flowx.applications.enums.UserStatus;

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
        user.setPassword(passwordEncoder.encode(randomPassword));

        user = userRepository.save(user);

        // publish event
        UserEvent.UserCreatedEvent userCreatedEvent = new UserEvent.UserCreatedEvent(user.getId(), user.getEmail(), randomPassword, user.getFullName(), user.getPosition());
        eventPublisher.publishEvent(userCreatedEvent);

        return userMapper.toUserResponse(user);
    }

    @Transactional
    @PreAuthorize("#id == authentication.principal.id or hasAnyAuthority('ROLE_MANAGER', 'ROLE_HR')")
    public UserResponse updateUser(UUID id, UserUpdateRequest userUpdateRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "User not found"));
        userMapper.updateUserFromRequest(user, userUpdateRequest);
        user = userRepository.save(user);

        return userMapper.toUserResponse(user);
    }

    @Transactional()
    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_HR')")
    public UserResponse updateUserStatus(UUID id, UserStatus status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "User not found"));
        user.setStatus(status);
        user = userRepository.save(user);
        return userMapper.toUserResponse(user);
    }

    @Transactional()
    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_HR')")
    public UserResponse updateUserPosition(UUID id, String position) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "User not found"));
        user.setPosition(position);
        user = userRepository.save(user);
        return userMapper.toUserResponse(user);
    }

    @Transactional
    @PreAuthorize("#id == authentication.principal.id or hasAnyAuthority('ROLE_MANAGER', 'ROLE_HR')")
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "User not found"));
        userRepository.delete(user);

        // publish event
        UserEvent.UserDeletedEvent userDeletedEvent = new UserEvent.UserDeletedEvent(user.getId());
        eventPublisher.publishEvent(userDeletedEvent);
    }

    /**
     * Internal method to update user avatar object key without security checks
     * Used by event handlers
     */
    @Transactional
    public void updateUserAvatarObjectKey(UUID userId, String objectKey) {
        log.debug("Starting avatar object key update for user {} with objectKey: {}", userId, objectKey);
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "User not found"));

            String oldAvatar = user.getAvatar();
            user.setAvatar(objectKey);
            userRepository.save(user);

            log.info("Successfully updated avatar object key for user {}: {} -> {}", userId, oldAvatar, objectKey);
        } catch (Exception e) {
            log.error("Error updating avatar object key for user {}: {}", userId, e.getMessage(), e);
            throw e; // Re-throw to ensure proper error handling
        }
    }

    /**
     * Internal method to update user background object key without security checks
     * Used by event handlers
     */
    @Transactional
    public void updateUserBackgroundObjectKey(UUID userId, String objectKey) {
        log.debug("Starting background object key update for user {} with objectKey: {}", userId, objectKey);
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "User not found"));

            String oldBackground = user.getBackground();
            user.setBackground(objectKey);
            userRepository.save(user);

            log.info("Successfully updated background object key for user {}: {} -> {}", userId, oldBackground, objectKey);
        } catch (Exception e) {
            log.error("Error updating background object key for user {}: {}", userId, e.getMessage(), e);
            throw e; // Re-throw to ensure proper error handling
        }
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return userMapper.toUserResponseList(users);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "User not found"));
        return userMapper.toUserResponse(user);
    }
}

