package project.ii.flowx.applications.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ii.flowx.applications.service.helper.MailService;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;
import project.ii.flowx.model.dto.auth.ChangePasswordRequest;
import project.ii.flowx.model.dto.auth.ForgotPasswordRequest;
import project.ii.flowx.model.dto.auth.ResetPasswordRequest;
import project.ii.flowx.model.entity.PasswordResetToken;
import project.ii.flowx.model.entity.User;
import project.ii.flowx.model.repository.PasswordResetTokenRepository;
import project.ii.flowx.model.repository.UserRepository;
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
        
        // Xóa các token cũ của user này
        passwordResetTokenRepository.deleteByUser(user);
        
        // Tạo token mới
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES));
        resetToken.setUsed(false);
        
        passwordResetTokenRepository.save(resetToken);
        
        // Gửi email
        try {
            mailService.sendPasswordResetEmail(user.getEmail(), token, user.getFullName());
        } catch (Exception e) {
            throw new FlowXException(FlowXError.INTERNAL_SERVER_ERROR, "Failed to send password reset email");
        }
    }
    
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String token = request.getToken();
        String newPassword = request.getNewPassword();
        
        PasswordResetToken resetToken = passwordResetTokenRepository.findValidToken(token, LocalDateTime.now())
                .orElseThrow(() -> new FlowXException(FlowXError.INVALID_TOKEN, "Invalid or expired reset token"));
        
        if (resetToken.isUsed())
            throw new FlowXException(FlowXError.INVALID_TOKEN, "Reset token has already been used");

        // Validate new password
        if (newPassword == null || newPassword.trim().length() < 6)
            throw new FlowXException(FlowXError.BAD_REQUEST, "Password must be at least 6 characters long");

        
        // Update password
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        // Mark token as used
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }

    @Transactional
    @PreAuthorize("isAuthenticated()")
    public void changePassword(ChangePasswordRequest changePasswordRequest) {
        Long userId = getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "User not found"));

        if(!passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPassword()))
            throw new FlowXException(FlowXError.ACCESS_DENIED, "Invalid password");

        user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void cleanupExpiredTokens() {
        passwordResetTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }

    private Long getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal userPrincipal)) {
            throw new FlowXException(FlowXError.ACCESS_DENIED, "Unauthorized");
        }
        return userPrincipal.getId();
    }

} 