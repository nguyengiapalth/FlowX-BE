package project.ii.flowx.module.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.ii.flowx.module.auth.entity.PasswordResetToken;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    
    /**
     * Find password reset token by token string
     */
    Optional<PasswordResetToken> findByToken(String token);
    
    /**
     * Find valid (unused and not expired) password reset token
     */
    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.token = :token AND prt.used = false AND prt.expiryDate > :now")
    Optional<PasswordResetToken> findValidToken(@Param("token") String token, @Param("now") LocalDateTime now);
    
    /**
     * Delete all password reset tokens for a specific user
     */
    void deleteByUserId(UUID userId);
    
    /**
     * Delete all expired password reset tokens and return count
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken prt WHERE prt.expiryDate < :now")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);
} 