package project.ii.flowx.module.content.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ii.flowx.applications.events.PostEvent;
import project.ii.flowx.module.auth.service.AuthorizationService;
import project.ii.flowx.module.file.FileService;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;
import project.ii.flowx.module.content.dto.post.*;
import project.ii.flowx.module.content.dto.comment.CommentResponse;
import project.ii.flowx.module.file.dto.FileResponse;
import project.ii.flowx.module.content.entity.Post;
import project.ii.flowx.module.content.mapper.PostMapper;
import project.ii.flowx.module.content.mapper.CommentMapper;
import project.ii.flowx.module.content.repository.PostRepository;
import project.ii.flowx.security.UserPrincipal;
import project.ii.flowx.applications.enums.Visibility;
import project.ii.flowx.applications.enums.FileTargetType;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostService {
    PostRepository postRepository;
    PostMapper postMapper;
    CommentMapper commentMapper;
    FileService fileService;

    AuthorizationService authorizationService;
    ApplicationEventPublisher eventPublisher;

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_MANAGER') " +
            "or @authorize.canAccessScope(#request.targetId, #request.visibility)")
    public PostResponse createPost(PostCreateRequest request) {
        UUID userId = getUserId();
        
        // Create post using mapper and set authorId
        Post post = postMapper.toPost(request);
        post.setAuthorId(userId); // Set authorId directly instead of User object
        post.setHasFile(false); // Will be updated later when files are uploaded
        
        post = postRepository.save(post);
        
        // Publish post created event using authorId directly
        eventPublisher.publishEvent(new PostEvent.PostCreatedEvent(
            post.getId(), 
            post.getAuthorId(), // Use authorId instead of author.getId()
            post.getSubtitle(),
            post.getVisibility().toString(),
            post.getTargetId()
        ));
        
        log.info("Created post {} by user {}", post.getId(), userId);
        
        PostResponse response = postMapper.toPostResponse(post);
        return populateFilesAndComments(response);
    }

    @Transactional
    @PreAuthorize("isAuthenticated()") // TODO: Implement @authorize.isPostAuthor(#id)
    public PostResponse updatePost(UUID id, PostUpdateRequest request) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Post not found"));
        
        postMapper.updatePostFromRequest(post, request);
        post = postRepository.save(post);
        
        log.info("Updated post {} by user {}", id, getUserId());
        
        PostResponse response = postMapper.toPostResponse(post);
        return populateFilesAndComments(response);
    }

    @Transactional
    public void updateHasFileFlag(UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Post not found"));

        try {
            List<FileResponse> files = fileService.getFilesByTarget(FileTargetType.POST, postId);
            boolean hasFiles = !files.isEmpty();
            
            if (post.isHasFile() != hasFiles) {
                post.setHasFile(hasFiles);
                postRepository.save(post);
                log.info("Updated hasFile flag for post {} to {}", postId, hasFiles);
            }
        } catch (Exception e) {
            log.error("Failed to update hasFile flag for post {}: {}", postId, e.getMessage());
        }
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public void deletePost(UUID id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Post not found"));
        
        // Delete associated files first
        try {
            fileService.deleteFilesByTarget(FileTargetType.POST, id);
        } catch (Exception e) {
            log.error("Failed to delete files for post {}: {}", id, e.getMessage());
        }
        
        postRepository.delete(post);
        log.info("Deleted post {} by user {}", id, getUserId());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<PostResponse> getGlobalPosts() {
        List<Post> posts = postRepository.findByVisibilityOrderByCreatedAtDesc(Visibility.GLOBAL);
        List<PostResponse> postResponses = postMapper.toPostResponseList(posts);
        return populateFilesAndCommentsList(postResponses);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authorize.hasProjectRole('MEMBER', #projectId)")
    public List<PostResponse> getProjectPosts(UUID projectId) {
        List<Post> posts = postRepository.findByVisibilityAndTargetIdOrderByCreatedAtDesc(Visibility.PROJECT, projectId);
        List<PostResponse> postResponses = postMapper.toPostResponseList(posts);
        return populateFilesAndCommentsList(postResponses);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<PostResponse> getPostsByUser(UUID userId) {
        // Fixed repository method name from findByAuthor_IdOrderByCreatedAtDesc to findByAuthorIdOrderByCreatedAtDesc
        List<Post> posts = postRepository.findByAuthorIdOrderByCreatedAtDesc(userId);
        List<PostResponse> postResponses = postMapper.toPostResponseList(posts);
        return populateFilesAndCommentsList(postResponses);
    }

    @Transactional(readOnly = true)
    @PostAuthorize("isAuthenticated()") // TODO: Implement @authorize.canAccessPost(#id)
    public PostResponse getPostById(UUID id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Post not found"));

        PostResponse response = postMapper.toPostResponse(post);
        return populateFilesAndComments(response);
    }

    // Helper method to get the current authenticated user's ID
    private UUID getUserId() {
        var context = SecurityContextHolder.getContext();
        if (context.getAuthentication() == null || context.getAuthentication().getPrincipal() == null) {
            throw new FlowXException(FlowXError.UNAUTHENTICATED, "No authenticated user found");
        }

        UserPrincipal userPrincipal = (UserPrincipal) context.getAuthentication().getPrincipal();
        return userPrincipal.getId();
    }

    private PostResponse populateFilesAndComments(PostResponse postResponse) {
        if (postResponse == null) return null;
        
        // Populate files
        try {
            if (postResponse.isHasFile()) {
                List<FileResponse> files = fileService.getFilesByTarget(FileTargetType.POST, postResponse.getId());
                postResponse.setFiles(files);
                
                if (files.isEmpty()) {
                    postResponse.setHasFile(false);
                    // Update database
                    Post post = postRepository.findById(postResponse.getId()).orElse(null);
                    if (post != null) {
                        post.setHasFile(false);
                        postRepository.save(post);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to populate files for post {}: {}", postResponse.getId(), e.getMessage());
        }
        
        // Populate comments
        try {
            List<CommentResponse> comments = commentMapper.toCommentResponseList(
                postRepository.findById(postResponse.getId())
                    .map(Post::getComments)
                    .orElse(List.of())
            );
            postResponse.setComments(comments);
        } catch (Exception e) {
            log.error("Failed to populate comments for post {}: {}", postResponse.getId(), e.getMessage());
        }
        
        return postResponse;
    }

    private List<PostResponse> populateFilesAndCommentsList(List<PostResponse> postResponses) {
        return postResponses.stream()
                .map(this::populateFilesAndComments)
                .collect(Collectors.toList());
    }

    public List<PostResponse> filterAccessiblePosts(List<PostResponse> posts) {
        return posts.stream()
                .filter(post -> authorizationService.canAccessPost(post.getId(), post.getVisibility()))
                .collect(Collectors.toList());
    }
} 