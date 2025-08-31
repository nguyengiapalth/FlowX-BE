package project.ii.flowx.applications.enums;

public enum ConverstationType {
    /**
        Direct conversation between two users
        This is typically used for private messages or one-on-one chats.
        Everybody can create a direct conversation with another user.
     */
    DIRECT,
    /**
        Group conversation with multiple users
        This is typically used for team discussions or group chats.
        Everybody can create a group conversation with multiple users.
     */
    GROUP,
    /**
        Project conversation for discussions related to a specific project
        This is typically used for project-related discussions.
        Only users who are part of the project can create or join this conversation.
     */
    PROJECT,
    /**
        Broadcast conversation for announcements to multiple users in a project or globally
        This is typically used for announcements or global messages.
        Only users with appropriate permissions can create a broadcast conversation.
     */
    BROADCAST,
    UNKNOWN;


    public static ConverstationType fromString(String type) {
        try {
            return ConverstationType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
