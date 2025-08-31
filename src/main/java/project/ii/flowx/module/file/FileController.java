package project.ii.flowx.module.file;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import project.ii.flowx.dto.Response;
import project.ii.flowx.module.file.dto.*;

import jakarta.validation.Valid;

import java.util.UUID;

@RestController
@RequestMapping("api")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "File", description = "File management API")
@SecurityRequirement(name = "bearerAuth")
public class FileController {
    FileService fileService;

    @Operation(
            summary = "Get presigned upload URL",
            description = "Generates a presigned URL for uploading a file directly to MinIO.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Presigned URL generated successfully"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request data"
                    )
            }
    )
    @PostMapping("/file/presigned-upload")
    public Response<PresignedUploadResponse> getPresignedUploadUrl(@Valid @RequestBody FileCreateRequest request) {
        log.info("Generate presigned upload URL request for file: {}", request.getName());
        return Response.<PresignedUploadResponse>builder()
                .code(200)
                .message("Presigned upload URL generated successfully")
                .data(fileService.getPresignedUploadUrl(request))
                .build();
    }

    @Operation(
            summary = "Delete file",
            description = "Deletes a file from storage and database.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "File deleted successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "File not found"
                    )
            }
    )
    @DeleteMapping("/file/{fileId}")
    public Response<Void> deleteFile(
            @Parameter(description = "File ID", required = true)
            @PathVariable UUID fileId)
    {
        log.info("Delete file request for file ID: {}", fileId);
        fileService.deleteFile(fileId);
        
        return Response.<Void>builder()
                .code(200)
                .message("File deleted successfully")
                .build();
    }

    @Operation(
            summary = "Confirm file upload",
            description = "Confirms that a file has been successfully uploaded to MinIO storage.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "File upload confirmed successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "File not found"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "File upload confirmation failed"
                    )
            }
    )
    @PostMapping("/file/confirm-upload/{fileId}")
    public Response<Void> confirmFileUpload(@PathVariable UUID fileId) {
        log.info("Confirm file upload request for file ID: {}", fileId);
        fileService.confirmFileUpload(fileId);
        return Response.<Void>builder()
                .code(200)
                .message("File upload confirmed successfully")
                .build();
    }
}
