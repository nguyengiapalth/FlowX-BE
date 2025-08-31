package project.ii.flowx.module.message.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import project.ii.flowx.module.message.service.MemberService;
import project.ii.flowx.module.message.service.MessageService;
import project.ii.flowx.module.message.dto.message.MessageCreateRequest;
import project.ii.flowx.module.message.dto.message.MessageResponse;
import project.ii.flowx.module.message.dto.message.MessageUpdateRequest;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.Header;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ConversationSocketController {
    MessageService messageService;
    MemberService memberService;
    SimpMessagingTemplate messagingTemplate;

    // Gửi tin nhắn mới
    @MessageMapping("/conversation.sendMessage")
    public void sendMessage(MessageCreateRequest request) {
        MessageResponse response = messageService.createMessage(request);
        messagingTemplate.convertAndSend("/topic/conversation/" + response.getConversationId(), response);
    }

    // Sửa tin nhắn
    @MessageMapping("/conversation.updateMessage")
    public void updateMessage(@Payload MessageUpdateRequest request, @Header("messageId") UUID messageId, Principal principal) {
        MessageResponse response = messageService.updateMessage(messageId, request);
        messagingTemplate.convertAndSend("/topic/conversation/" + response.getConversationId(), response);
    }

    @MessageMapping("/conversation.readAt")
    public void readMessage(@Header("conversationId") UUID conversationId, @Header("userId") UUID userId, @Header("readAt") String readAt) {
        // Gọi memberService để cập nhật readAt
        // memberService.updateReadAt(conversationId, userId, readAt); // Cần bổ sung hàm này trong MemberService
        // Broadcast sự kiện đã đọc nếu cần
        messagingTemplate.convertAndSend("/topic/conversation/" + conversationId + "/read", userId);
    }

    // Xóa tin nhắn (cập nhật messageDeleteMap nếu không phải tin nhắn của mình)
    @MessageMapping("/conversation.deleteMessage")
    public void deleteMessage(@Header("messageId") UUID messageId, Principal principal) {
        MessageResponse message = messageService.getMessage(messageId);
        UUID senderId = message.getSenderId();
        UUID currentUserId = null;
        if (principal != null && principal.getName() != null) {
            try {
                currentUserId = UUID.fromString(principal.getName());
            } catch (Exception ignored) {}
        }
        messageService.deleteMessage(messageId);
        if (currentUserId != null && !currentUserId.equals(senderId)) {
            // Nếu không phải tin nhắn của mình, cập nhật messageDeleteMap cho member
            // memberService.updateMessageDeleteMap(message.getConversationId(), currentUserId, messageId);
        }
        messagingTemplate.convertAndSend("/topic/conversation/message-deleted", messageId);
    }
}