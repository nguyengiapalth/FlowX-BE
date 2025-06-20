package project.ii.flowx.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import project.ii.flowx.applications.service.FileService;
import project.ii.flowx.model.dto.FlowXResponse;
import project.ii.flowx.model.dto.file.*;
import project.ii.flowx.shared.enums.FileTargetType;

import jakarta.validation.Valid;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            summary = "Get presigned download URL",
            description = "Generates a presigned URL for downloading a file.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Presigned URL generated successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "File not found"
                    )
            }
    )
    @GetMapping("/file/presigned-download/{fileId}")
    public FlowXResponse<String> getPresignedDownloadUrl(@PathVariable Long fileId) {
        log.info("Generate presigned download URL request for file ID: {}", fileId);
        String presignedUrl = fileService.getPresignedDownloadUrl(fileId);

        return FlowXResponse.<String>builder()
                .code(200)
                .message("Presigned download URL generated successfully")
                .data(presignedUrl)
                .build();
    }

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
    public FlowXResponse<PresignedUploadResponse> getPresignedUploadUrl(@Valid @RequestBody FileCreateRequest request) {
        log.info("Generate presigned upload URL request for file: {}", request.getName());
        return FlowXResponse.<PresignedUploadResponse>builder()
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
    public FlowXResponse<Void> deleteFile(
            @Parameter(description = "File ID", required = true)
            @PathVariable Long fileId)
    {

        log.info("Delete file request for file ID: {}", fileId);
        
        fileService.deleteFile(fileId);
        
        return FlowXResponse.<Void>builder()
                .code(200)
                .message("File deleted successfully")
                .build();
    }

    @Operation(
            summary = "Batch delete files",
            description = "Deletes multiple files at once.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Files deleted successfully"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request data"
                    )
            }
    )
    @DeleteMapping("/file/batch")
    public FlowXResponse<Map<String, Object>> batchDeleteFiles(@Valid @RequestBody BatchDeleteRequest request) {

        log.info("Batch delete files request for {} files", request.getFileIds().size());

        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failedCount = 0;

        for (Long fileId : request.getFileIds()) {
            try {
                fileService.deleteFile(fileId);
                successCount++;
            } catch (Exception e) {
                log.error("Failed to delete file ID: {}, error: {}", fileId, e.getMessage());
                failedCount++;
            }
        }

        result.put("successCount", successCount);
        result.put("failedCount", failedCount);
        result.put("totalRequested", request.getFileIds().size());

        return FlowXResponse.<Map<String, Object>>builder()
                .code(200)
                .message(String.format("Batch delete completed: %d successful, %d failed", successCount, failedCount))
                .data(result)
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
    public FlowXResponse<Void> confirmFileUpload(@PathVariable Long fileId) {
        log.info("Confirm file upload request for file ID: {}", fileId);
        fileService.confirmFileUpload(fileId);
        return FlowXResponse.<Void>builder()
                .code(200)
                .message("File upload confirmed successfully")
                .build();
    }

}
