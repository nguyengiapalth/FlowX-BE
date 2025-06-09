package project.ii.flowx.applications.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ii.flowx.applications.service.helper.MailService;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;
import project.ii.flowx.model.dto.auth.ForgotPasswordRequest;
import project.ii.flowx.model.dto.auth.ResetPasswordRequest;
import project.ii.flowx.model.entity.PasswordResetToken;
import project.ii.flowx.model.entity.User;
import project.ii.flowx.model.repository.PasswordResetTokenRepository;
import project.ii.flowx.model.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {
    
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;
    
    private static final int TOKEN_EXPIRY_MINUTES = 15;
    
    @Transactional
    public void sendPasswordResetEmail(ForgotPasswordRequest request) {
        String email = request.getEmail();
        log.info("Processing password reset request for email: {}", email);
        
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
            log.info("Password reset email sent successfully to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send password reset email", e);
            throw new FlowXException(FlowXError.INTERNAL_SERVER_ERROR, "Failed to send password reset email");
        }
    }
    
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String token = request.getToken();
        String newPassword = request.getNewPassword();
        
        log.info("Processing password reset with token: {}", token);
        
        PasswordResetToken resetToken = passwordResetTokenRepository.findValidToken(token, LocalDateTime.now())
                .orElseThrow(() -> new FlowXException(FlowXError.INVALID_TOKEN, "Invalid or expired reset token"));
        
        if (resetToken.isUsed()) {
            throw new FlowXException(FlowXError.INVALID_TOKEN, "Reset token has already been used");
        }
        
        // Validate new password
        if (newPassword == null || newPassword.trim().length() < 6) {
            throw new FlowXException(FlowXError.BAD_REQUEST, "Password must be at least 6 characters long");
        }
        
        // Update password
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        // Mark token as used
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
        
        log.info("Password reset successfully for user: {}", user.getEmail());
    }
    
    @Transactional
    public void cleanupExpiredTokens() {
        passwordResetTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        log.info("Expired password reset tokens cleaned up");
    }
} 