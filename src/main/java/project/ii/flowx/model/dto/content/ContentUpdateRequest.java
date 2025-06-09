package project.ii.flowx.model.dto.content;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Schema(description = "Content Update Request")
public class ContentUpdateRequest {
    @Schema(description = "Body or content details", example = "This is the updated content of my post")
    String body;
}