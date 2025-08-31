package project.ii.flowx.module.message.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ii.flowx.module.message.dto.conversation.ConversationCreateRequest;
import project.ii.flowx.module.message.dto.conversation.ConversationResponse;
import project.ii.flowx.module.message.entity.Conversation;
import project.ii.flowx.module.message.repository.ConversationRepository;
import project.ii.flowx.module.message.mapper.ConversationMapper;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConversationService {
    ConversationRepository conversationRepository;
    ConversationMapper conversationMapper;

    @Transactional
    public ConversationResponse createConversation(ConversationCreateRequest request) {
        Conversation conversation = conversationMapper.toConversation(request);
        conversation = conversationRepository.save(conversation);
        return conversationMapper.toConversationResponse(conversation);
    }

    @Transactional
    public void deleteConversation(UUID id) {
        Conversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Conversation not found"));
        conversationRepository.delete(conversation);
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> getMyConversations() {
        ///  get my id
        return conversationMapper.toConversationResponseList(conversationRepository.findAll());
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> getConversationByProjectId(UUID id) {
        List<Conversation> conversations = conversationRepository.findByProjectId(id);
        if (conversations.isEmpty()) {
            throw new FlowXException(FlowXError.NOT_FOUND, "No conversations found for project ID: " + id);
        }
        return conversationMapper.toConversationResponseList(conversations);
    }
} 