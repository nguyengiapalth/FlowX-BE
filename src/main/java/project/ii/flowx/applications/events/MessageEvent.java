package project.ii.flowx.applications.events;

import java.util.UUID;

/**
 * Message-related events for the application
 */
public class MessageEvent {

    /**
     * Event fired when a new message is created
     */
    public static class MessageCreatedEvent {
        private final UUID messageId;
        private final UUID senderId;
        private final UUID conversationId;
        private final String content;

        public MessageCreatedEvent(UUID messageId, UUID senderId, UUID conversationId, String content) {
            this.messageId = messageId;
            this.senderId = senderId;
            this.conversationId = conversationId;
            this.content = content;
        }

        public UUID getMessageId() {
            return messageId;
        }

        public UUID getSenderId() {
            return senderId;
        }

        public UUID getConversationId() {
            return conversationId;
        }

        public String getContent() {
            return content;
        }
    }

    /**
     * Event fired when a message is updated
     */
    public static class MessageUpdatedEvent {
        private final UUID messageId;
        private final UUID senderId;
        private final UUID conversationId;

        public MessageUpdatedEvent(UUID messageId, UUID senderId, UUID conversationId) {
            this.messageId = messageId;
            this.senderId = senderId;
            this.conversationId = conversationId;
        }

        public UUID getMessageId() {
            return messageId;
        }

        public UUID getSenderId() {
            return senderId;
        }

        public UUID getConversationId() {
            return conversationId;
        }
    }

    /**
     * Event fired when a message is deleted
     */
    public static class MessageDeletedEvent {
        private final UUID messageId;
        private final UUID senderId;
        private final UUID conversationId;

        public MessageDeletedEvent(UUID messageId, UUID senderId, UUID conversationId) {
            this.messageId = messageId;
            this.senderId = senderId;
            this.conversationId = conversationId;
        }

        public UUID getMessageId() {
            return messageId;
        }

        public UUID getSenderId() {
            return senderId;
        }

        public UUID getConversationId() {
            return conversationId;
        }
    }
} 