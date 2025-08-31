package project.ii.flowx.module.message.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import project.ii.flowx.dto.Response;
import project.ii.flowx.module.message.dto.message.MessageCreateRequest;
import project.ii.flowx.module.message.dto.message.MessageResponse;
import project.ii.flowx.module.message.dto.message.MessageUpdateRequest;
import project.ii.flowx.module.message.service.MessageService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Message", description = "Message API for chat functionality")
@SecurityRequirement(name = "bearerAuth")
public class MessageController {
    MessageService messageService;

    @Operation(
            summary = "Create a new message",
            description = "Creates a new message in a conversation. The message will be broadcast to all conversation members.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Message created successfully"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid message details provided"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "User not authorized to send message to this conversation"
                    )
            }
    )
    @PostMapping
    public Response<MessageResponse> createMessage(
            @Valid @RequestBody MessageCreateRequest request) {
        log.info("Creating message for conversation: {}", request.getConversationId());
        MessageResponse response = messageService.createMessage(request);
        return Response.<MessageResponse>builder()
                .code(201)
                .message("Message created successfully")
                .data(response)
                .build();
    }

    @Operation(
            summary = "Update a message",
            description = "Updates an existing message. Only the message sender can update the message.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Message updated successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Message not found"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "User not authorized to update this message"
                    )
            }
    )
    @PutMapping("/{id}")
    public Response<MessageResponse> updateMessage(
            @Parameter(description = "Message ID") @PathVariable UUID id,
            @Valid @RequestBody MessageUpdateRequest request) {
        log.info("Updating message: {}", id);
        MessageResponse response = messageService.updateMessage(id, request);
        return Response.<MessageResponse>builder()
                .code(200)
                .message("Message updated successfully")
                .data(response)
                .build();
    }

    @Operation(
            summary = "Get messages by conversation",
            description = "Retrieves all messages in a conversation. Messages are ordered by creation time.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Messages retrieved successfully"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "User not authorized to view this conversation"
                    )
            }
    )
    @GetMapping("/conversation/{conversationId}")
    public Response<List<MessageResponse>> getMessagesByConversation(
            @Parameter(description = "Conversation ID") @PathVariable UUID conversationId) {
        log.info("Getting messages for conversation: {}", conversationId);
        List<MessageResponse> messages = messageService.getMessagesByConversation(conversationId);
        return Response.<List<MessageResponse>>builder()
                .code(200)
                .message("Messages retrieved successfully")
                .data(messages)
                .build();
    }

    @Operation(
            summary = "Delete a message",
            description = "Soft deletes a message. Only the message sender can delete the message.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Message deleted successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Message not found"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "User not authorized to delete this message"
                    )
            }
    )
    @DeleteMapping("/{id}")
    public Response<Void> deleteMessage(
            @Parameter(description = "Message ID") @PathVariable UUID id) {
        log.info("Deleting message: {}", id);
        messageService.deleteMessage(id);
        return Response.<Void>builder()
                .code(200)
                .message("Message deleted successfully")
                .build();
    }

    @Operation(
            summary = "Mark message as read",
            description = "Marks a message as read by the current user.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Message marked as read"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Message not found"
                    )
            }
    )
    @PatchMapping("/{id}/read")
    public Response<Void> markMessageAsRead(
            @Parameter(description = "Message ID") @PathVariable UUID id) {
        log.info("Marking message as read: {}", id);
        messageService.markAsRead(id);
        return Response.<Void>builder()
                .code(200)
                .message("Message marked as read")
                .build();
    }
} 