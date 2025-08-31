package project.ii.flowx.applications.eventhandlers;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import project.ii.flowx.applications.events.PostEvent;
import project.ii.flowx.module.notify.NotificationService;
import project.ii.flowx.applications.helper.EntityLookupService;
import project.ii.flowx.module.notify.dto.NotificationCreateRequest;
import project.ii.flowx.module.content.entity.Post;
import project.ii.flowx.module.content.entity.Comment;
import project.ii.flowx.module.user.entity.User;
import project.ii.flowx.module.content.repository.PostRepository;
import project.ii.flowx.module.content.repository.CommentRepository;

@Component
@EnableAsync
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostEventHandler {
    NotificationService notificationService;
    EntityLookupService entityLookupService;
    PostRepository postRepository;
    CommentRepository commentRepository;

    @EventListener
    @Async
    public void handlePostCreated(PostEvent.PostCreatedEvent event) {
        log.info("Post created: {}", event);
        
        try {
            // Create notification for followers or project members (if applicable)
            // This is a placeholder - you might want to implement follower/member logic
            // For now, we'll just log the event
            log.info("Post {} created by user {} with title: {}", 
                event.postId(), event.authorId(), event.title());
        } catch (Exception e) {
            log.error("Error handling post created event: {}", e.getMessage(), e);
        }
    }

    @EventListener
    @Async
    public void handleCommentCreated(PostEvent.CommentCreatedEvent event) {
        log.info("Comment created: {}", event);
        
        try {
            // Notify post author about new comment
            Post post = postRepository.findById(event.postId()).orElse(null);
            if (post != null && !post.getAuthorId().equals(event.authorId())) {
                User commentAuthor = entityLookupService.getUserById(event.authorId());
                
                NotificationCreateRequest notification = NotificationCreateRequest.builder()
                    .userId(post.getAuthorId())
                    .title("New Comment on Your Post")
                    .content(commentAuthor.getFullName() + " commented on your post: \"" + 
                        (event.content().length() > 100 ? 
                            event.content().substring(0, 100) + "..." : 
                            event.content()) + "\"")
                    .build();
                
                notificationService.createNotification(notification);
                log.info("Notification sent to post author {} for comment on post {}", 
                    post.getAuthorId(), event.postId());
            }
        } catch (Exception e) {
            log.error("Error handling comment created event: {}", e.getMessage(), e);
        }
    }

    @EventListener
    @Async
    public void handleCommentReply(PostEvent.CommentReplyEvent event) {
        log.info("Comment reply created: {}", event);
        
        try {
            // Notify parent comment author about reply
            if (!event.parentAuthorId().equals(event.authorId())) {
                User replyAuthor = entityLookupService.getUserById(event.authorId());
                
                NotificationCreateRequest notification = NotificationCreateRequest.builder()
                    .userId(event.parentAuthorId())
                    .title("New Reply to Your Comment")
                    .content(replyAuthor.getFullName() + " replied to your comment: \"" + 
                        (event.content().length() > 100 ? 
                            event.content().substring(0, 100) + "..." : 
                            event.content()) + "\"")
                    .build();
                
                notificationService.createNotification(notification);
                log.info("Notification sent to comment author {} for reply {}", 
                    event.parentAuthorId(), event.replyId());
            }
        } catch (Exception e) {
            log.error("Error handling comment reply event: {}", e.getMessage(), e);
        }
    }

    @EventListener
    @Async
    public void handlePostReaction(PostEvent.PostReactionEvent event) {
        log.info("Post reaction: {}", event);
        
        try {
            // Optionally notify post author about reactions
            Post post = postRepository.findById(event.postId()).orElse(null);
            if (post != null && !post.getAuthorId().equals(event.userId())) {
                User reactingUser = entityLookupService.getUserById(event.userId());
                
                // Only send notification for likes (to avoid spam)
                if ("LIKE".equals(event.reactionType())) {
                    NotificationCreateRequest notification = NotificationCreateRequest.builder()
                        .userId(post.getAuthorId())
                        .title("Someone liked your post")
                        .content(reactingUser.getFullName() + " liked your post")
                        .build();
                    
                    notificationService.createNotification(notification);
                }
            }
        } catch (Exception e) {
            log.error("Error handling post reaction event: {}", e.getMessage(), e);
        }
    }

    @EventListener
    @Async
    public void handleCommentReaction(PostEvent.CommentReactionEvent event) {
        log.info("Comment reaction: {}", event);
        
        try {
            // Optionally notify comment author about reactions
            Comment comment = commentRepository.findById(event.commentId()).orElse(null);
            if (comment != null && !comment.getAuthorId().equals(event.userId())) {
                User reactingUser = entityLookupService.getUserById(event.userId());
                
                // Only send notification for likes (to avoid spam)
                if ("LIKE".equals(event.reactionType())) {
                    NotificationCreateRequest notification = NotificationCreateRequest.builder()
                        .userId(comment.getAuthorId())
                        .title("Someone liked your comment")
                        .content(reactingUser.getFullName() + " liked your comment")
                        .build();
                    
                    notificationService.createNotification(notification);
                }
            }
        } catch (Exception e) {
            log.error("Error handling comment reaction event: {}", e.getMessage(), e);
        }
    }
}
