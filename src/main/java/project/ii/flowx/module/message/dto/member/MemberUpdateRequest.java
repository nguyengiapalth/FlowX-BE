package project.ii.flowx.module.message.dto.member;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;

@Schema(description = "ConversationMember Update Request")
@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class MemberUpdateRequest {
    @Schema(description = "Role of the member in the conversation", example = "MEMBER")
    String role;
}