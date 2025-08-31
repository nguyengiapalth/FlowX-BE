package project.ii.flowx.module.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.ii.flowx.module.auth.entity.InvalidToken;

import java.util.UUID;

@Repository
public interface InvalidTokenRepository extends JpaRepository<InvalidToken, UUID> {
    boolean existsByToken(String token);
}
