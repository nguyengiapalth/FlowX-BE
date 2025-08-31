package project.ii.flowx.module.message.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Schema(description = "Message Update Request")
@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class MessageUpdateRequest {
    @Size(max = 2000, message = "Content cannot exceed 2000 characters")
    @Schema(description = "Content of the message", example = "Hello!")
    String content;

    @Schema(description = "Status of the message", example = "READ")
    String status;
} 