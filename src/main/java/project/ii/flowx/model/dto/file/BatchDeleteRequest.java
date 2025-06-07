package project.ii.flowx.model.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "Batch Delete Files Request")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class BatchDeleteRequest {
    @NotEmpty(message = "File IDs list cannot be empty")
    @Size(max = 100, message = "Cannot delete more than 100 files at once")
    @Schema(description = "List of file IDs to delete", example = "[1, 2, 3]")
    List<Long> fileIds;
} 