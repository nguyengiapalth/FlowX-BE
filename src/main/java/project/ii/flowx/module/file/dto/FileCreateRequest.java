package project.ii.flowx.module.file.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import project.ii.flowx.applications.enums.FileTargetType;
import project.ii.flowx.applications.enums.FileVisibility;

import java.util.UUID;

@Schema(description = "File Create Request")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class FileCreateRequest {
    @NotBlank(message = "File name is required")
    @Size(min = 1, max = 255, message = "File name must be between 1 and 255 characters")
    @Schema(description = "Name of the file", example = "document.pdf")
    String name;

    @NotBlank(message = "File type is required")
    @Schema(description = "Type of the file", example = "application/pdf")
    String type;

    @NotNull(message = "File size is required")
    @Min(value = 1, message = "File size must be greater than 0")
    @Schema(description = "Size of the file in bytes", example = "1024")
    Long size;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Schema(description = "Description of the file", example = "Project documentation")
    String description;

    @Schema(description = "ID of the related entity", example = "5")
    UUID targetId;

    @NotNull(message = "File target type is required")
    @Schema(description = "Type of the related entity", example = "CONTENT")
    @Enumerated(EnumType.STRING)
    FileTargetType fileTargetType;

    @NotNull(message = "File visibility is required")
    @Schema(description = "Visibility of the file", example = "PRIVATE")
    @Enumerated(EnumType.STRING)
    FileVisibility visibility;
}
