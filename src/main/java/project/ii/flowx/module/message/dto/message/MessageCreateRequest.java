package project.ii.flowx.module.message.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import java.util.UUID;

@Schema(description = "Message Create Request")
@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class MessageCreateRequest {
    @NotNull(message = "Conversation ID is required")
    @Schema(description = "ID of the conversation", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID conversationId;

    @NotBlank(message = "Content is required")
    @Size(min = 1, max = 10000, message = "Content must be between 1 and 10000 characters")
    @Schema(description = "Content of the message", example = "Hello!", maxLength = 10000)
    String content;
} 