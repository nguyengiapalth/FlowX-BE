package project.ii.flowx.model.dto.content;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Schema(description = "Content Create Request")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ContentCreateRequest {
    @Schema(description = "Title of the content", example = "My First Post")
    @NotNull
    String title;

    @Schema(description = "Body or content details", example = "This is the main content of my post")
    String body;

    @Schema(description = "Parent content if this is a reply or nested content")
    long parentId;

    @Schema(description = "Depth level of the content in a hierarchical structure", example = "0")
    int depth;
}
