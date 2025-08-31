package project.ii.flowx.module.message.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ii.flowx.applications.events.MessageEvent;
import project.ii.flowx.applications.helper.EntityLookupService;
import project.ii.flowx.module.auth.service.AuthorizationService;
import project.ii.flowx.module.message.dto.message.MessageCreateRequest;
import project.ii.flowx.module.message.dto.message.MessageUpdateRequest;
import project.ii.flowx.module.message.dto.message.MessageResponse;
import project.ii.flowx.module.message.entity.Message;
import project.ii.flowx.module.message.entity.Conversation;
import project.ii.flowx.module.user.entity.User;
import project.ii.flowx.module.message.repository.MessageRepository;
import project.ii.flowx.module.message.repository.ConversationRepository;
import project.ii.flowx.module.message.mapper.MessageMapper;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;
import project.ii.flowx.security.UserPrincipal;
import project.ii.flowx.applications.enums.MessageStatus;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MessageService {
    MessageRepository messageRepository;
    ConversationRepository conversationRepository;
    MessageMapper messageMapper;
    SimpMessagingTemplate messagingTemplate;
    EntityLookupService entityLookupService;
    AuthorizationService authorizationService;
    ApplicationEventPublisher eventPublisher;

    @Transactional
    @PreAuthorize("@authorize.canSendMessageToConversation(#request.conversationId)")
    public MessageResponse createMessage(MessageCreateRequest request) {
        log.info("Creating message for conversation: {}", request.getConversationId());
        
        UUID userId = getUserId();
        User sender = entityLookupService.getUserById(userId);
        Conversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Conversation not found"));

        Message message = messageMapper.toMessage(request);
        message.setSender(sender);
        message.setConversation(conversation);
        message.setStatus(MessageStatus.SENT);
        
        message = messageRepository.save(message);
        log.info("Message created with ID: {}", message.getId());

        // Publish message created event
        eventPublisher.publishEvent(new MessageEvent.MessageCreatedEvent(
            message.getId(),
            message.getSender().getId(),
            message.getConversation().getId(),
            message.getContent()
        ));

        // Broadcast the new message to the conversation topic
        MessageResponse response = messageMapper.toMessageResponse(message);
        messagingTemplate.convertAndSend("/topic/conversation/" + 
            conversation.getId(), response);
        
        return response;
    }

    @Transactional
    @PreAuthorize("@authorize.isMessageOwner(#id)")
    public MessageResponse updateMessage(UUID id, MessageUpdateRequest request) {
        log.info("Updating message: {}", id);
        
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Message not found"));
        
        messageMapper.updateMessageFromRequest(message, request);
        message = messageRepository.save(message);
        
        log.info("Message updated: {}", id);

        // Broadcast the updated message to the conversation topic
        MessageResponse response = messageMapper.toMessageResponse(message);
        messagingTemplate.convertAndSend("/topic/conversation/" + 
            message.getConversation().getId() + "/update", response);
        
        return response;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authorize.canViewConversation(#conversationId)")
    public List<MessageResponse> getMessagesByConversation(UUID conversationId) {
        log.info("Getting messages for conversation: {}", conversationId);
        
        // Verify conversation exists
        conversationRepository.findById(conversationId)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Conversation not found"));

        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        List<MessageResponse> responses = messages.stream()
                .map(messageMapper::toMessageResponse)
                .collect(Collectors.toList());
        
        log.info("Retrieved {} messages for conversation: {}", responses.size(), conversationId);
        return responses;
    }

    @Transactional
    @PreAuthorize("@authorize.isMessageOwner(#id)")
    public void deleteMessage(UUID id) {
        log.info("Deleting message: {}", id);
        
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Message not found"));
        
        // Soft delete by updating status
        message.setStatus(MessageStatus.DELETED);
        messageRepository.save(message);
        
        log.info("Message soft deleted: {}", id);

        // Broadcast deletion event to the conversation topic
        messagingTemplate.convertAndSend("/topic/conversation/" + 
            message.getConversation().getId() + "/delete", id);
    }

    @Transactional
    @PreAuthorize("@authorize.canViewMessage(#id)")
    public void markAsRead(UUID id) {
        log.info("Marking message as read: {}", id);
        
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Message not found"));
        
        // Only mark as read if not already read and user is not the sender
        UUID currentUserId = getUserId();
        if (!message.getSender().getId().equals(currentUserId) && 
            message.getStatus() != MessageStatus.READ) {
            
            message.setStatus(MessageStatus.READ);
            messageRepository.save(message);
            
            log.info("Message marked as read: {}", id);

            // Broadcast read status to conversation
            messagingTemplate.convertAndSend("/topic/conversation/" + 
                message.getConversation().getId() + "/read", 
                Map.of("messageId", id, "readBy", currentUserId));
        }
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authorize.canViewMessage(#id)")
    public MessageResponse getMessage(UUID id) {
        log.info("Getting message: {}", id);
        
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Message not found"));
        
        return messageMapper.toMessageResponse(message);
    }

    private UUID getUserId() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return userPrincipal.getId();
    }
}