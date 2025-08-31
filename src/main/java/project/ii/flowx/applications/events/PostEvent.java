package project.ii.flowx.applications.events;

import java.util.UUID;

public class PostEvent {

    // Post Events
    public record PostCreatedEvent(UUID postId, UUID authorId, String title, String visibility, UUID targetId) {}

    // Comment Events
    public record CommentCreatedEvent(UUID commentId, UUID postId, UUID authorId, String content, UUID parentCommentId) {}

    // Reply Events (for nested comments)
    public record CommentReplyEvent(UUID replyId, UUID parentCommentId, UUID postId, UUID authorId, UUID parentAuthorId, String content) {}

    // Reaction Events
    public record PostReactionEvent(UUID postId, UUID userId, String reactionType) {}

    public record CommentReactionEvent(UUID commentId, UUID postId, UUID userId, String reactionType) {}
}
