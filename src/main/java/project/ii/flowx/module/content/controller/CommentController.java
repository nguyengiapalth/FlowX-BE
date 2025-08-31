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
import project.ii.flowx.module.content.service.CommentService;
import project.ii.flowx.dto.Response;
import project.ii.flowx.module.content.dto.comment.CommentCreateRequest;
import project.ii.flowx.module.content.dto.comment.CommentResponse;
import project.ii.flowx.module.content.dto.comment.CommentUpdateRequest;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/comment")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Comment", description = "Comment API")
public class CommentController {
    CommentService commentService;

    @Operation(
            summary = "Create a new comment",
            description = "Creates a new comment on a post.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Comment created successfully"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid comment details provided"
                    )
            }
    )
    @PostMapping("/create")
    public Response<CommentResponse> create(@RequestBody CommentCreateRequest request) {
        log.info("Creating comment with body: {} on post: {}", request.getBody(), request.getPostId());
        return Response.<CommentResponse>builder()
                .data(commentService.createComment(request))
                .message("Comment created successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Update comment",
            description = "Updates the details of an existing comment.",
            parameters = {
                    @Parameter(name = "id", description = "ID of the comment to be updated")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Comment updated successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Comment not found"
                    )
            }
    )
    @PutMapping("/update/{id}")
    public Response<CommentResponse> update(@PathVariable UUID id, @RequestBody CommentUpdateRequest request) {
        return Response.<CommentResponse>builder()
                .data(commentService.updateComment(id, request))
                .message("Comment updated successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Delete comment",
            description = "Deletes a comment by its unique ID.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Comment deleted successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Comment not found"
                    )
            }
    )
    @DeleteMapping("/delete/{id}")
    public Response<Void> delete(@PathVariable UUID id) {
        commentService.deleteComment(id);
        return Response.<Void>builder()
                .message("Comment deleted successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Get comments by post",
            description = "Retrieves all comments for a specific post.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of comments retrieved successfully"
                    )
            }
    )
    @GetMapping("/post/{postId}")
    public Response<List<CommentResponse>> getCommentsByPost(@PathVariable UUID postId) {
        return Response.<List<CommentResponse>>builder()
                .data(commentService.getCommentsByPost(postId))
                .message("List of comments by post retrieved successfully")
                .code(200)
                .build();
    }

//    @Operation(
//            summary = "Get replies by comment",
//            description = "Retrieves all replies to a specific comment.",
//            responses = {
//                    @ApiResponse(
//                            responseCode = "200",
//                            description = "List of replies retrieved successfully"
//                    )
//            }
//    )
//    @GetMapping("/replies/{commentId}")
//    public Response<List<CommentResponse>> getRepliesByComment(@PathVariable UUID commentId) {
//        return Response.<List<CommentResponse>>builder()
//                .data(commentService.getRepliesByComment(commentId))
//                .message("List of replies retrieved successfully")
//                .code(200)
//                .build();
//    }

} 