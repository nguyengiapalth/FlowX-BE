package project.ii.flowx.module.content.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ii.flowx.applications.events.PostEvent;
import project.ii.flowx.module.file.FileService;
import project.ii.flowx.applications.helper.EntityLookupService;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;
import project.ii.flowx.module.content.dto.comment.*;
import project.ii.flowx.module.file.dto.FileResponse;
import project.ii.flowx.module.content.entity.Comment;
import project.ii.flowx.module.content.entity.Post;
import project.ii.flowx.module.content.mapper.CommentMapper;
import project.ii.flowx.module.content.repository.CommentRepository;
import project.ii.flowx.module.content.repository.PostRepository;
import project.ii.flowx.security.UserPrincipal;
import project.ii.flowx.applications.enums.FileTargetType;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentService {
    CommentRepository commentRepository;
    PostRepository postRepository;
    CommentMapper commentMapper;
    EntityLookupService entityLookupService;
    FileService fileService;
    ApplicationEventPublisher eventPublisher;

    @Transactional
    @PreAuthorize("isAuthenticated()") // TODO: Check if user can comment on the post
    public CommentResponse createComment(CommentCreateRequest request) {
        UUID userId = getUserId();

        // Get the post
        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Post not found"));

        // Create comment using mapper and set authorId
        Comment comment = commentMapper.toComment(request);
        comment.setAuthorId(userId); // Set authorId directly instead of User object
        comment.setPost(post);
        comment.setHasFile(false);

        // Handle parent comment and depth
        if (request.getParentId() != null) {
            Comment parentComment = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Parent comment not found"));
            
            // Limit nesting depth to avoid infinite nesting
            if (parentComment.getDepth() >= 2) {
                comment.setDepth(parentComment.getDepth());
                comment.setParent(parentComment.getParent());
            } else {
                comment.setDepth(parentComment.getDepth() + 1);
                comment.setParent(parentComment);
            }
        } else {
            comment.setDepth(1); // Top-level comment
            comment.setParent(null);
        }

        comment = commentRepository.save(comment);

        // Publish comment created event using authorId directly
        eventPublisher.publishEvent(new PostEvent.CommentCreatedEvent(
            comment.getId(),
            comment.getPost().getId(),
            comment.getAuthorId(), // Use authorId instead of author.getId()
            comment.getBody(),
            comment.getParent() != null ? comment.getParent().getId() : null
        ));

        // Publish reply event if this is a reply
        if (comment.getParent() != null) {
            eventPublisher.publishEvent(new PostEvent.CommentReplyEvent(
                comment.getId(),
                comment.getParent().getId(),
                comment.getPost().getId(),
                comment.getAuthorId(), // Use authorId instead of author.getId()
                comment.getParent().getAuthorId(), // Use parent's authorId instead of author.getId()
                comment.getBody()
            ));
        }

        log.info("Created comment {} by user {} on post {}", comment.getId(), userId, post.getId());

        CommentResponse response = commentMapper.toCommentResponse(comment);
        return populateFiles(response);
    }

    @Transactional
    @PreAuthorize("isAuthenticated()") // TODO: Implement @authorize.isCommentAuthor(#id)
    public CommentResponse updateComment(UUID id, CommentUpdateRequest request) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Comment not found"));

        commentMapper.updateCommentFromRequest(comment, request);
        comment = commentRepository.save(comment);
        
        log.info("Updated comment {} by user {}", id, getUserId());
        
        CommentResponse response = commentMapper.toCommentResponse(comment);
        return populateFiles(response);
    }

    @Transactional
    public void updateHasFileFlag(UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Comment not found"));

        try {
            List<FileResponse> files = fileService.getFilesByTarget(FileTargetType.COMMENT, commentId);
            boolean hasFiles = !files.isEmpty();

            if (comment.isHasFile() != hasFiles) {
                comment.setHasFile(hasFiles);
                commentRepository.save(comment);
                log.info("Updated hasFile flag for comment {} to {}", commentId, hasFiles);
            }
        } catch (Exception e) {
            log.error("Failed to update hasFile flag for comment {}: {}", commentId, e.getMessage());
        }
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_MANAGER')") // Only managers can delete comments
    public void deleteComment(UUID id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Comment not found"));
        
        // Delete associated files first
        try {
            fileService.deleteFilesByTarget(FileTargetType.COMMENT, id);
        } catch (Exception e) {
            log.error("Failed to delete files for comment {}: {}", id, e.getMessage());
        }
        
        commentRepository.delete(comment);
        log.info("Deleted comment {} by user {}", id, getUserId());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<CommentResponse> getCommentsByPost(UUID postId) {
        // Get top-level comments (depth = 1) and their replies
        List<Comment> comments = commentRepository.findByPostIdAndDepthOrderByCreatedAtAsc(postId, 1);
        List<CommentResponse> commentResponses = commentMapper.toCommentResponseList(comments);
        return populateFilesList(commentResponses);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<CommentResponse> getCommentsByUser(UUID userId) {
        List<Comment> comments = commentRepository.findByAuthorIdOrderByCreatedAtDesc(userId);
        List<CommentResponse> commentResponses = commentMapper.toCommentResponseList(comments);
        return populateFilesList(commentResponses);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public CommentResponse getCommentById(UUID id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "Comment not found"));

        CommentResponse response = commentMapper.toCommentResponse(comment);
        return populateFiles(response);
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

    private CommentResponse populateFiles(CommentResponse commentResponse) {
        if (commentResponse == null) return null;
        
        try {
            if (commentResponse.isHasFile()) {
                List<FileResponse> files = fileService.getFilesByTarget(FileTargetType.COMMENT, commentResponse.getId());
                commentResponse.setFiles(files);
                
                if (files.isEmpty()) {
                    commentResponse.setHasFile(false);
                    // Update database
                    Comment comment = commentRepository.findById(commentResponse.getId()).orElse(null);
                    if (comment != null) {
                        comment.setHasFile(false);
                        commentRepository.save(comment);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to populate files for comment {}: {}", commentResponse.getId(), e.getMessage());
        }
        
        return commentResponse;
    }

    private List<CommentResponse> populateFilesList(List<CommentResponse> commentResponses) {
        return commentResponses.stream()
                .map(this::populateFiles)
                .collect(Collectors.toList());
    }
} 