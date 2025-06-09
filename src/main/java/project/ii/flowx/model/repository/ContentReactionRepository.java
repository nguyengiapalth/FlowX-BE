package project.ii.flowx.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.ii.flowx.model.entity.ContentReaction;
import project.ii.flowx.shared.enums.ReactionType;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContentReactionRepository extends JpaRepository<ContentReaction, Long> {
    
    Optional<ContentReaction> findByContentIdAndUserId(Long contentId, Long userId);
    
    List<ContentReaction> findByContentIdOrderByCreatedAtDesc(Long contentId);
    
    @Query("SELECT COUNT(cr) FROM ContentReaction cr WHERE cr.content.id = :contentId")
    Long countByContentId(@Param("contentId") Long contentId);
    
    @Query("SELECT cr.reactionType, COUNT(cr) FROM ContentReaction cr WHERE cr.content.id = :contentId GROUP BY cr.reactionType")
    List<Object[]> countReactionsByTypeAndContentId(@Param("contentId") Long contentId);
    
    @Query("SELECT cr FROM ContentReaction cr WHERE cr.content.id = :contentId AND cr.reactionType = :reactionType ORDER BY cr.createdAt DESC")
    List<ContentReaction> findByContentIdAndReactionTypeOrderByCreatedAtDesc(@Param("contentId") Long contentId, @Param("reactionType") ReactionType reactionType);
    
    void deleteByContentIdAndUserId(Long contentId, Long userId);
    
    boolean existsByContentIdAndUserId(Long contentId, Long userId);
} 