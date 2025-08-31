package project.ii.flowx.module.message.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.ii.flowx.module.message.entity.ConversationMember;

import java.util.List;
import java.util.UUID;

@Repository
public interface MemberRepository extends JpaRepository<ConversationMember, UUID> {
    List<ConversationMember> findByConversationId(UUID conversationId);
}