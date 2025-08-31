package project.ii.flowx.module.content.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.ii.flowx.module.content.entity.Comment;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {
    
    /**
     * Find top-level comments by post ID and depth
     */
    List<Comment> findByPostIdAndDepthOrderByCreatedAtAsc(UUID postId, int depth);
    
    /**
     * Find comments by post ID and parent is null (top-level comments)
     */
    List<Comment> findByPostIdAndParentIsNullOrderByCreatedAtAsc(UUID postId);
    
    /**
     * Find replies by parent comment ID
     */
    List<Comment> findByParentIdOrderByCreatedAtAsc(UUID parentId);
    
    /**
     * Find comments by author ID
     */
    List<Comment> findByAuthorIdOrderByCreatedAtDesc(UUID authorId);
    
    /**
     * Count comments by post ID
     */
    long countByPostId(UUID postId);
    
    /**
     * Find all comments by post ID ordered by creation time
     */
    List<Comment> findByPostIdOrderByCreatedAtAsc(UUID postId);
} 