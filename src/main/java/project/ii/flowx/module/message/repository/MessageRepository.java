package project.ii.flowx.module.message.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.ii.flowx.module.message.entity.Message;
import project.ii.flowx.applications.enums.MessageStatus;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {
    
    /**
     * Find messages by conversation ID, excluding deleted messages
     */
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId AND m.status != 'DELETED' ORDER BY m.createdAt ASC")
    List<Message> findByConversationIdOrderByCreatedAtAsc(@Param("conversationId") UUID conversationId);
    
    /**
     * Find messages by conversation ID and status
     */
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId AND m.status = :status ORDER BY m.createdAt DESC")
    List<Message> findByConversationIdAndStatus(@Param("conversationId") UUID conversationId, @Param("status") MessageStatus status);
    
    /**
     * Count unread messages in a conversation for a specific user
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation.id = :conversationId AND m.status = 'SENT' AND m.senderId != :userId")
    Long countUnreadMessagesByConversationAndUser(@Param("conversationId") UUID conversationId, @Param("userId") UUID userId);
    
    /**
     * Find messages by sender ID
     */
    @Query("SELECT m FROM Message m WHERE m.senderId = :senderId ORDER BY m.createdAt DESC")
    List<Message> findBySenderIdOrderByCreatedAtDesc(@Param("senderId") UUID senderId);
    
    /**
     * Find messages by sender ID using repository method naming
     */
    List<Message> findBySenderIdOrderByCreatedAtAsc(UUID senderId);
} 