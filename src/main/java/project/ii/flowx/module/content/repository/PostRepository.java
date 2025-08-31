package project.ii.flowx.module.content.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.ii.flowx.module.content.entity.Post;
import project.ii.flowx.applications.enums.Visibility;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    /**
     * Find posts by visibility and target ID ordered by creation time
     */
    List<Post> findByVisibilityAndTargetIdOrderByCreatedAtDesc(Visibility visibility, UUID targetId);

    /**
     * Find posts by visibility ordered by creation time
     */
    List<Post> findByVisibilityOrderByCreatedAtDesc(Visibility visibility);

    /**
     * Find posts by author ID ordered by creation time
     */
    List<Post> findByAuthorIdOrderByCreatedAtDesc(UUID authorId);

    /**
     * Find all posts ordered by creation time
     */
    List<Post> findAllByOrderByCreatedAtDesc();
}