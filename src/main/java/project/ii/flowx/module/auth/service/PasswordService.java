package project.ii.flowx.module.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ii.flowx.applications.helper.MailService;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;
import project.ii.flowx.module.auth.dto.auth.ChangePasswordRequest;
import project.ii.flowx.module.auth.dto.auth.ForgotPasswordRequest;
import project.ii.flowx.module.auth.dto.auth.ResetPasswordRequest;
import project.ii.flowx.module.auth.entity.PasswordResetToken;
import project.ii.flowx.module.user.entity.User;
import project.ii.flowx.module.auth.repository.PasswordResetTokenRepository;
import project.ii.flowx.module.user.repository.UserRepository;
import project.ii.flowx.security.UserPrincipal;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for handling password-related operations such as resetting and changing passwords.
 * It includes methods to send password reset emails, reset passwords using tokens,
 * and change the user's password.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordService {
    
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;
    
    private static final int TOKEN_EXPIRY_MINUTES = 15;
    
    @Transactional
    public void sendPasswordResetEmail(ForgotPasswordRequest request) {
        String email = request.getEmail();
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "User not found with email: " + email));
        
        UUID userId = user.getId();

        String token = UUID.randomUUID().toString();

        // Delete old tokens for this user
        passwordResetTokenRepository.deleteByUserId(userId);
        
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .userId(userId) // Use userId instead of user object
                .expiryDate(LocalDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES))
                .used(false)
                .build();
                new PasswordResetToken();

        passwordResetTokenRepository.save(resetToken);
        
        // Send email
        try {
            mailService.sendPasswordResetEmail(user.getEmail(), token, user.getFullName());
            log.info("Password reset email sent to user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to send password reset email to user: {}", userId, e);
            throw new FlowXException(FlowXError.INTERNAL_SERVER_ERROR, "Failed to send password reset email");
        }
    }
    
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String token = request.getToken();
        String newPassword = request.getNewPassword();
        
        PasswordResetToken resetToken = passwordResetTokenRepository.findValidToken(token, LocalDateTime.now())
                .orElseThrow(() -> new FlowXException(FlowXError.INVALID_TOKEN, "Invalid or expired reset token"));
        
        if (resetToken.isUsed()) {
            throw new FlowXException(FlowXError.INVALID_TOKEN, "Reset token has already been used");
        }

        // Get user by userId from token
        UUID userId = resetToken.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "User not found"));
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        // Mark token as used
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
        
        log.info("Password reset successfully for user: {}", userId);
    }

    @Transactional
    @PreAuthorize("isAuthenticated()")
    public void changePassword(ChangePasswordRequest request) {
        UUID userId = getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "User not found"));

        // Validate old password
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new FlowXException(FlowXError.ACCESS_DENIED, "Invalid old password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        log.info("Password changed successfully for user: {}", userId);
    }

    private UUID getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal userPrincipal)) {
            throw new FlowXException(FlowXError.ACCESS_DENIED, "Unauthorized");
        }
        return userPrincipal.getId();
    }
} 