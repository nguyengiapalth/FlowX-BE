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
import project.ii.flowx.applications.helper.EntityLookupService;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;
import project.ii.flowx.module.content.dto.reaction.*;
import project.ii.flowx.module.content.entity.Comment;
import project.ii.flowx.module.content.entity.Post;
import project.ii.flowx.module.content.entity.Reaction;
import project.ii.flowx.module.content.mapper.ReactionMapper;
import project.ii.flowx.module.content.repository.ContentReactionRepository;
import project.ii.flowx.security.UserPrincipal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReactionService {
    ContentReactionRepository contentReactionRepository;
    ReactionMapper reactionMapper;
    EntityLookupService entityLookupService;
    ApplicationEventPublisher eventPublisher;

    private UUID getUserId() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userPrincipal.getId();
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ReactionResponse addOrUpdateReaction(ReactionCreateRequest request) {
        UUID userId = getUserId();
        
        // Validate that either postId or commentId is provided, but not both
        if ((request.getPostId() == null && request.getCommentId() == null) ||
            (request.getPostId() != null && request.getCommentId() != null)) {
            throw new FlowXException(FlowXError.BAD_REQUEST, "Either postId or commentId must be provided, but not both");
        }
        
        Optional<Reaction> existingReaction;
        Reaction reaction;
        Post post = null;
        Comment comment = null;
        
        if (request.getPostId() != null) {
            // Reacting to a post
            post = entityLookupService.getPostById(request.getPostId());
            existingReaction = contentReactionRepository.findByPostAndUserId(post, userId);
            
            if (existingReaction.isPresent()) {
                // Update existing reaction
                reaction = existingReaction.get();
                reaction.setReactionType(request.getReactionType());
                log.info("Updated reaction for post {} by user {}", post.getId(), userId);
            } else {
                // Create new reaction using builder and set userId directly
                reaction = Reaction.builder()
                    .post(post)
                    .userId(userId) // Use userId instead of user object
                    .reactionType(request.getReactionType())
                    .build();
                log.info("Created new reaction for post {} by user {}", post.getId(), userId);
            }
            
            reaction = contentReactionRepository.save(reaction);
            
            // Publish event
            eventPublisher.publishEvent(new PostEvent.PostReactionEvent(
                post.getId(), 
                userId, 
                request.getReactionType().toString()
            ));
            
        } else {
            // Reacting to a comment
            comment = entityLookupService.getCommentById(request.getCommentId());
            post = comment.getPost(); // Get the post for event publishing
            existingReaction = contentReactionRepository.findByCommentAndUserId(comment, userId);
            
            if (existingReaction.isPresent()) {
                // Update existing reaction
                reaction = existingReaction.get();
                reaction.setReactionType(request.getReactionType());
                log.info("Updated reaction for comment {} by user {}", comment.getId(), userId);
            } else {
                // Create new reaction using builder and set userId directly
                reaction = Reaction.builder()
                    .comment(comment)
                    .userId(userId) // Use userId instead of user object
                    .reactionType(request.getReactionType())
                    .build();
                log.info("Created new reaction for comment {} by user {}", comment.getId(), userId);
            }
            
            reaction = contentReactionRepository.save(reaction);
            
            // Publish event
            eventPublisher.publishEvent(new PostEvent.CommentReactionEvent(
                comment.getId(),
                post.getId(),
                userId, 
                request.getReactionType().toString()
            ));
        }
        
        return reactionMapper.toReactionResponse(reaction);
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public void removeReaction(UUID postId, UUID commentId) {
        UUID userId = getUserId();
        
        // Validate that either postId or commentId is provided, but not both
        if ((postId == null && commentId == null) || (postId != null && commentId != null)) {
            throw new FlowXException(FlowXError.BAD_REQUEST, "Either postId or commentId must be provided, but not both");
        }
        
        if (postId != null) {
            // Remove reaction from post
            Post post = entityLookupService.getPostById(postId);
            if (!contentReactionRepository.existsByPostAndUserId(post, userId)) {
                throw new FlowXException(FlowXError.NOT_FOUND, "Reaction not found");
            }
            contentReactionRepository.deleteByPostAndUserId(post, userId);
            log.info("Removed reaction for post {} by user {}", postId, userId);
        } else {
            // Remove reaction from comment
            Comment comment = entityLookupService.getCommentById(commentId);
            if (!contentReactionRepository.existsByCommentAndUserId(comment, userId)) {
                throw new FlowXException(FlowXError.NOT_FOUND, "Reaction not found");
            }
            contentReactionRepository.deleteByCommentAndUserId(comment, userId);
            log.info("Removed reaction for comment {} by user {}", commentId, userId);
        }
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<ReactionResponse> getReactionsByPost(UUID postId) {
        Post post = entityLookupService.getPostById(postId);
        List<Reaction> reactions = contentReactionRepository.findByPost(post);
        return reactionMapper.toReactionResponseList(reactions);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<ReactionResponse> getReactionsByComment(UUID commentId) {
        Comment comment = entityLookupService.getCommentById(commentId);
        List<Reaction> reactions = contentReactionRepository.findByComment(comment);
        return reactionMapper.toReactionResponseList(reactions);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<ReactionResponse> getReactionsByUser(UUID userId) {
        List<Reaction> reactions = contentReactionRepository.findByUserId(userId);
        return reactionMapper.toReactionResponseList(reactions);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public ReactionSummary getReactionSummaryForPost(UUID postId) {
        Post post = entityLookupService.getPostById(postId);
        List<Reaction> reactions = contentReactionRepository.findByPost(post);
        
        return buildReactionSummary(reactions);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public ReactionSummary getReactionSummaryForComment(UUID commentId) {
        Comment comment = entityLookupService.getCommentById(commentId);
        List<Reaction> reactions = contentReactionRepository.findByComment(comment);
        
        return buildReactionSummary(reactions);
    }

    private ReactionSummary buildReactionSummary(List<Reaction> reactions) {
        ReactionSummary summary = new ReactionSummary();
        
        for (Reaction reaction : reactions) {
            switch (reaction.getReactionType()) {
                case LIKE -> summary.incrementLikes();
                case DISLIKE -> summary.incrementDislikes();
                case LOVE -> summary.incrementLoves();
                case ANGRY -> summary.incrementAngry();
                case LAUGH -> summary.incrementLaughs();
                case SAD -> summary.incrementSad();
            }
        }
        
        return summary;
    }
} 