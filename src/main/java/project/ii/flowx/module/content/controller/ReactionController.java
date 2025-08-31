package project.ii.flowx.module.content.controller;

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
import project.ii.flowx.dto.Response;
import project.ii.flowx.module.content.dto.reaction.ReactionCreateRequest;
import project.ii.flowx.module.content.dto.reaction.ReactionResponse;
import project.ii.flowx.module.content.dto.reaction.ReactionSummary;
import project.ii.flowx.module.content.service.ReactionService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("api/reaction")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Reaction", description = "Reaction API")
public class ReactionController {
    ReactionService reactionService;

    @Operation(
            summary = "Add or update a reaction",
            description = "Creates a new reaction or updates an existing one on a post or comment.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Reaction added/updated successfully"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid reaction details provided"
                    )
            }
    )
    @PostMapping("/react")
    public Response<ReactionResponse> addOrUpdateReaction(@RequestBody ReactionCreateRequest request) {
        log.info("Adding/updating reaction: {} on post: {} comment: {}", 
                request.getReactionType(), request.getPostId(), request.getCommentId());
        return Response.<ReactionResponse>builder()
                .data(reactionService.addOrUpdateReaction(request))
                .message("Reaction added/updated successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Remove reaction from post",
            description = "Removes the current user's reaction from a post.",
            parameters = {
                    @Parameter(name = "postId", description = "ID of the post to remove reaction from")
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
    @DeleteMapping("/post/{postId}")
    public Response<Void> removePostReaction(@PathVariable UUID postId) {
        reactionService.removeReaction(postId, null);
        return Response.<Void>builder()
                .message("Reaction removed successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Remove reaction from comment",
            description = "Removes the current user's reaction from a comment.",
            parameters = {
                    @Parameter(name = "commentId", description = "ID of the comment to remove reaction from")
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
    @DeleteMapping("/comment/{commentId}")
    public Response<Void> removeCommentReaction(@PathVariable UUID commentId) {
        reactionService.removeReaction(null, commentId);
        return Response.<Void>builder()
                .message("Reaction removed successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Get reactions by post",
            description = "Retrieves all reactions for a specific post.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of reactions retrieved successfully"
                    )
            }
    )
    @GetMapping("/post/{postId}")
    public Response<List<ReactionResponse>> getReactionsByPost(@PathVariable UUID postId) {
        return Response.<List<ReactionResponse>>builder()
                .data(reactionService.getReactionsByPost(postId))
                .message("List of reactions by post retrieved successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Get reactions by comment",
            description = "Retrieves all reactions for a specific comment.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of reactions retrieved successfully"
                    )
            }
    )
    @GetMapping("/comment/{commentId}")
    public Response<List<ReactionResponse>> getReactionsByComment(@PathVariable UUID commentId) {
        return Response.<List<ReactionResponse>>builder()
                .data(reactionService.getReactionsByComment(commentId))
                .message("List of reactions by comment retrieved successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Get post reaction summary",
            description = "Retrieves a summary of reactions for a specific post including counts by type.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Reaction summary retrieved successfully"
                    )
            }
    )
    @GetMapping("/summary/post/{postId}")
    public Response<ReactionSummary> getPostReactionSummary(@PathVariable UUID postId) {
        return Response.<ReactionSummary>builder()
                .data(reactionService.getPostReactionSummary(postId))
                .message("Post reaction summary retrieved successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Get comment reaction summary",
            description = "Retrieves a summary of reactions for a specific comment including counts by type.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Reaction summary retrieved successfully"
                    )
            }
    )
    @GetMapping("/summary/comment/{commentId}")
    public Response<ReactionSummary> getCommentReactionSummary(@PathVariable UUID commentId) {
        return Response.<ReactionSummary>builder()
                .data(reactionService.getCommentReactionSummary(commentId))
                .message("Comment reaction summary retrieved successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Get user's post reaction",
            description = "Retrieves the current user's reaction on a specific post.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User reaction retrieved successfully"
                    )
            }
    )
    @GetMapping("/user/post/{postId}")
    public Response<Optional<ReactionResponse>> getUserPostReaction(@PathVariable UUID postId) {
        return Response.<Optional<ReactionResponse>>builder()
                .data(reactionService.getUserPostReaction(postId))
                .message("User post reaction retrieved successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Get user's comment reaction",
            description = "Retrieves the current user's reaction on a specific comment.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User reaction retrieved successfully"
                    )
            }
    )
    @GetMapping("/user/comment/{commentId}")
    public Response<Optional<ReactionResponse>> getUserCommentReaction(@PathVariable UUID commentId) {
        return Response.<Optional<ReactionResponse>>builder()
                .data(reactionService.getUserCommentReaction(commentId))
                .message("User comment reaction retrieved successfully")
                .code(200)
                .build();
    }
} 