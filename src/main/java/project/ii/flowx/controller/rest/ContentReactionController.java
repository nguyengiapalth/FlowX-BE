package project.ii.flowx.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import project.ii.flowx.applications.service.manage.ContentReactionService;
import project.ii.flowx.model.dto.FlowXResponse;
import project.ii.flowx.model.dto.content.ContentReactionRequest;
import project.ii.flowx.model.dto.content.ContentReactionResponse;
import project.ii.flowx.model.dto.content.ContentReactionSummary;
import project.ii.flowx.shared.enums.ReactionType;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/content/reactions")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Content Reactions", description = "Content Reaction API")
public class ContentReactionController {
    ContentReactionService contentReactionService;

    @Operation(
            summary = "Add or update reaction to content",
            description = "Adds a new reaction or updates existing reaction to a content. If user already reacted, the reaction type will be updated.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Reaction added/updated successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Content not found"
                    )
            }
    )
    @PostMapping("/react")
    public FlowXResponse<ContentReactionResponse> addOrUpdateReaction(@Valid @RequestBody ContentReactionRequest request) {
        log.info("Adding/updating reaction for content {} with type {}", request.getContentId(), request.getReactionType());
        return FlowXResponse.<ContentReactionResponse>builder()
                .data(contentReactionService.addOrUpdateReaction(request))
                .message("Reaction added/updated successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Remove reaction from content",
            description = "Removes the current user's reaction from a content.",
            parameters = {
                    @Parameter(name = "contentId", description = "ID of the content")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Reaction removed successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Reaction not found"
                    )
            }
    )
    @DeleteMapping("/{contentId}")
    public FlowXResponse<Void> removeReaction(@PathVariable Long contentId) {
        log.info("Removing reaction for content {}", contentId);
        contentReactionService.removeReaction(contentId);
        return FlowXResponse.<Void>builder()
                .message("Reaction removed successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Get all reactions for a content",
            description = "Retrieves all reactions for a specific content, ordered by creation time (newest first).",
            parameters = {
                    @Parameter(name = "contentId", description = "ID of the content")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of reactions retrieved successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Content not found"
                    )
            }
    )
    @GetMapping("/content/{contentId}")
    public FlowXResponse<List<ContentReactionResponse>> getReactionsByContent(@PathVariable Long contentId) {
        return FlowXResponse.<List<ContentReactionResponse>>builder()
                .data(contentReactionService.getReactionsByContent(contentId))
                .message("List of reactions retrieved successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Get reactions by content and type",
            description = "Retrieves all reactions of a specific type for a content.",
            parameters = {
                    @Parameter(name = "contentId", description = "ID of the content"),
                    @Parameter(name = "reactionType", description = "Type of reaction to filter by")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of reactions retrieved successfully"
                    )
            }
    )
    @GetMapping("/content/{contentId}/type/{reactionType}")
    public FlowXResponse<List<ContentReactionResponse>> getReactionsByContentAndType(
            @PathVariable Long contentId, 
            @PathVariable ReactionType reactionType) {
        return FlowXResponse.<List<ContentReactionResponse>>builder()
                .data(contentReactionService.getReactionsByContentAndType(contentId, reactionType))
                .message("List of reactions retrieved successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Get reaction summary for content",
            description = "Retrieves a summary of reactions for a content, including total count, count by type, and current user's reaction.",
            parameters = {
                    @Parameter(name = "contentId", description = "ID of the content")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Reaction summary retrieved successfully"
                    )
            }
    )
    @GetMapping("/content/{contentId}/summary")
    public FlowXResponse<ContentReactionSummary> getReactionSummary(@PathVariable Long contentId) {
        return FlowXResponse.<ContentReactionSummary>builder()
                .data(contentReactionService.getReactionSummary(contentId))
                .message("Reaction summary retrieved successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Get current user's reaction",
            description = "Retrieves the current user's reaction for a specific content, if any.",
            parameters = {
                    @Parameter(name = "contentId", description = "ID of the content")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User reaction retrieved successfully (may be null if no reaction)"
                    )
            }
    )
    @GetMapping("/content/{contentId}/my-reaction")
    public FlowXResponse<ContentReactionResponse> getUserReaction(@PathVariable Long contentId) {
        Optional<ContentReactionResponse> userReaction = contentReactionService.getUserReaction(contentId);
        return FlowXResponse.<ContentReactionResponse>builder()
                .data(userReaction.orElse(null))
                .message(userReaction.isPresent() ? "User reaction retrieved successfully" : "No reaction found")
                .code(200)
                .build();
    }
} 