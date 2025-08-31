package project.ii.flowx.module.message.dto.conversation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Schema(description = "Conversation Create Request")
@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ConversationCreateRequest {
    @NotBlank(message = "Conversation name is required")
    @Size(min = 1, max = 255, message = "Conversation name must be between 1 and 255 characters")
    @Schema(description = "Name of the conversation", example = "Team Chat")
    String name;

    @Schema(description = "Background of the conversation", example = "#FFFFFF or https://...")
    String background;
} 