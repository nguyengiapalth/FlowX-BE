package project.ii.flowx.model.dto.content;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import project.ii.flowx.shared.enums.ContentTargetType;

import java.time.Instant;
import java.util.List;

@Schema(description = "Content Response")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContentResponse {
    String title;
    String body;
    ContentTargetType contentTargetType;
    Long targetId;
    long parentId;
    int depth;
    Instant createdAt;
    Instant updatedAt;
    List<ContentResponse> replies;
}