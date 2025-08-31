package project.ii.flowx.module.message.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import project.ii.flowx.dto.Response;
import project.ii.flowx.module.message.service.ConversationService;
import project.ii.flowx.module.message.dto.conversation.ConversationCreateRequest;
import project.ii.flowx.module.message.dto.conversation.ConversationResponse;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConversationController {
    ConversationService conversationService;

    @PostMapping
    public Response<ConversationResponse> create(@RequestBody ConversationCreateRequest request) {
        ConversationResponse response = conversationService.createConversation(request);
        return Response.<ConversationResponse>builder()
                .code(201)
                .message("Conversation created successfully")
                .data(response)
                .build();
    }

    @DeleteMapping("/{id}")
    public Response<Void> delete(@PathVariable UUID id) {
        conversationService.deleteConversation(id);
        return Response.<Void>builder()
                .code(204)
                .message("Conversation deleted successfully")
                .data(null)
                .build();
    }

    @GetMapping
    public Response<List<ConversationResponse>> getMyConversation() {
        List<ConversationResponse> responses = conversationService.getMyConversations();
        return Response.<List<ConversationResponse>>builder()
                .code(200)
                .message("All conversations retrieved successfully")
                .data(responses)
                .build();
    }
} 