package project.ii.flowx.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.ii.flowx.model.entity.InvalidToken;

@Repository
public interface InvalidTokenRepository extends JpaRepository<InvalidToken, Long> {
    boolean existsByToken(String token);
}
