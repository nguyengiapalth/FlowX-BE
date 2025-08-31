package project.ii.flowx.module.message.dto.member;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import java.util.UUID;

@Schema(description = "ConversationMember Create Request")
@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class MemberCreateRequest {
    @NotNull(message = "Conversation ID is required")
    @Schema(description = "ID of the conversation", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID conversationId;

    @NotNull(message = "User ID is required")
    @Schema(description = "ID of the user", example = "123e4567-e89b-12d3-a456-426614174001")
    UUID userId;

    @NotNull(message = "Role is required")
    @Schema(description = "Role of the member in the conversation", example = "MEMBER")
    String role;

    @Size(max = 100, message = "Nickname cannot exceed 100 characters")
    @Schema(description = "Nickname of the member in the conversation", example = "Giap")
    String nickname;
} 