package project.ii.flowx.module.content.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.ii.flowx.module.content.entity.Reaction;
import project.ii.flowx.module.content.entity.Post;
import project.ii.flowx.module.content.entity.Comment;
import project.ii.flowx.applications.enums.ReactionType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContentReactionRepository extends JpaRepository<Reaction, UUID> {
    
    /**
     * Find reactions by post ordered by creation time
     */
    List<Reaction> findByPost(Post post);
    
    /**
     * Find reactions by comment ordered by creation time
     */
    List<Reaction> findByComment(Comment comment);
    
    /**
     * Find reactions by user ID
     */
    List<Reaction> findByUserId(UUID userId);
    
    /**
     * Find user's reaction on a post
     */
    Optional<Reaction> findByPostAndUserId(Post post, UUID userId);
    
    /**
     * Find user's reaction on a comment
     */
    Optional<Reaction> findByCommentAndUserId(Comment comment, UUID userId);
    
    /**
     * Count reactions by post
     */
    long countByPost(Post post);
    
    /**
     * Count reactions by comment
     */
    long countByComment(Comment comment);
    
    /**
     * Count reactions by post and type
     */
    long countByPostAndReactionType(Post post, ReactionType reactionType);
    
    /**
     * Count reactions by comment and type
     */
    long countByCommentAndReactionType(Comment comment, ReactionType reactionType);
    
    /**
     * Get reaction counts by type for a post
     */
    @Query("SELECT r.reactionType, COUNT(r) FROM Reaction r WHERE r.post = :post GROUP BY r.reactionType")
    List<Object[]> countReactionsByTypeAndPost(@Param("post") Post post);
    
    /**
     * Get reaction counts by type for a comment
     */
    @Query("SELECT r.reactionType, COUNT(r) FROM Reaction r WHERE r.comment = :comment GROUP BY r.reactionType")
    List<Object[]> countReactionsByTypeAndComment(@Param("comment") Comment comment);
    
    /**
     * Delete user's reaction on a post
     */
    void deleteByPostAndUserId(Post post, UUID userId);
    
    /**
     * Delete user's reaction on a comment
     */
    void deleteByCommentAndUserId(Comment comment, UUID userId);
    
    /**
     * Check if user already reacted to a post
     */
    boolean existsByPostAndUserId(Post post, UUID userId);
    
    /**
     * Check if user already reacted to a comment
     */
    boolean existsByCommentAndUserId(Comment comment, UUID userId);
}