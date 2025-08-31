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
import project.ii.flowx.module.content.service.PostService;
import project.ii.flowx.module.content.dto.post.PostCreateRequest;
import project.ii.flowx.module.content.dto.post.PostResponse;
import project.ii.flowx.module.content.dto.post.PostUpdateRequest;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/post")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Post", description = "Post API")
public class PostController {
    PostService postService;

    @Operation(
            summary = "Create a new post",
            description = "Creates a new post in the system.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Post created successfully"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid post details provided"
                    )
            }
    )
    @PostMapping("/create")
    public Response<PostResponse> createPost(@RequestBody PostCreateRequest request) {
        log.info("Creating post with body: {}", request.getBody());
        return Response.<PostResponse>builder()
                .data(postService.createPost(request))
                .message("Post created successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Update post",
            description = "Updates the details of an existing post.",
            parameters = {
                    @Parameter(name = "id", description = "ID of the post to be updated")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Post updated successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Post not found"
                    )
            }
    )
    @PutMapping("/update/{id}")
    public Response<PostResponse> updatePost(@PathVariable UUID id, @RequestBody PostUpdateRequest request) {
        return Response.<PostResponse>builder()
                .data(postService.updatePost(id, request))
                .message("Post updated successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Delete post",
            description = "Deletes a post by its unique ID.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Post deleted successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Post not found"
                    )
            }
    )
    @DeleteMapping("/delete/{id}")
    public Response<Void> deletePost(@PathVariable UUID id) {
        postService.deletePost(id);
        return Response.<Void>builder()
                .message("Post deleted successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Get post by ID",
            description = "Retrieves a post by its unique ID.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Post retrieved successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Post not found"
                    )
            }
    )
    @GetMapping("/get/{id}")
    public Response<PostResponse> getPostById(@PathVariable UUID id) {
        return Response.<PostResponse>builder()
                .data(postService.getPostById(id))
                .message("Post retrieved successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Get all posts",
            description = "Retrieves a list of all posts in the system.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of posts retrieved successfully"
                    )
            }
    )
    @GetMapping("/get-all")
    public Response<List<PostResponse>> getPosts() {
        log.info("Retrieving all posts");
        return Response.<List<PostResponse>>builder()
                .data(postService.filterAccessiblePosts(postService.getAllPosts()))
                .message("List of posts retrieved successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Get posts by target",
            description = "Retrieves a list of posts associated with a specific target type and ID.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of posts retrieved successfully"
                    )
            }
    )
    @GetMapping("/project/{projectId}")
    public Response<List<PostResponse>> getPostsByProject(
            @PathVariable UUID projectId) {
        return Response.<List<PostResponse>>builder()
                .data(postService.filterAccessiblePosts(
                        postService.getPostsByProject(projectId)))
                .message("List of target posts retrieved successfully")
                .code(200)
                .build();
    }

    @GetMapping("/user/{userId}")
    @Operation(
            summary = "Get posts by user",
            description = "Retrieves a list of posts created by a specific user.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of posts retrieved successfully"
                    )
            }
    )
    public Response<List<PostResponse>> getPostsByUser(@PathVariable UUID userId) {
        return Response.<List<PostResponse>>builder()
                .data(postService.getPostsByUser(userId))
                .message("List of posts by user retrieved successfully")
                .code(200)
                .build();
    }
} 