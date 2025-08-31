package project.ii.flowx.module.file.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Schema(description = "Presigned Upload Response")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PresignedUploadResponse {
    private UUID presignedFileId;
    private String url;
}
