package project.ii.flowx.module.message.dto.member;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "ConversationMember Response")
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class MemberResponse {
    UUID id;
    UUID conversationId;
    UUID userId;
    String role;
    String nickname;
    LocalDateTime joinedAt;
    LocalDateTime readAt;
    String messageDeleteMap;
} 