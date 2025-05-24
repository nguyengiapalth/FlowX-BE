package project.ii.flowx.model.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.ii.flowx.model.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {


    Optional<User> findByEmail(String email);

    List<User> findByDepartmentId(Long departmentId);

    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = {"department"})
    Optional<User> findUserById(Long id);

}
