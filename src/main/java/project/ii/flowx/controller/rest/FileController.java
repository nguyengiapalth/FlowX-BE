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
import org.springframework.web.bind.annotation.*;
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
@RequestMapping("api/file")
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
    @GetMapping("/presigned-download/{fileId}")
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
    @PostMapping("/presigned-upload")
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
    @DeleteMapping("/{fileId}")
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
            summary = "Get file information",
            description = "Retrieves file metadata by ID.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "File information retrieved successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "File not found"
                    )
            }
    )
    @GetMapping("/{fileId}")
    public FlowXResponse<FileResponse> getFileInfo(
            @PathVariable Long fileId)
    {

        log.info("Get file info request for file ID: {}", fileId);
        
        FileResponse response = fileService.getFileInfo(fileId);
        
        return FlowXResponse.<FileResponse>builder()
                .code(200)
                .message("File information retrieved successfully")
                .data(response)
                .build();
    }

    @Operation(
            summary = "Get files by entity",
            description = "Retrieves all files associated with a specific entity.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Files retrieved successfully"
                    )
            }
    )
    @GetMapping("/entity/{entityType}/{entityId}")
    public FlowXResponse<List<FileResponse>> getFilesByEntity(@PathVariable FileTargetType fileTargetType, @PathVariable Long entityId)
    {

        log.info("Get files by entity request - Type: {}, ID: {}", fileTargetType, entityId);
        
        List<FileResponse> files = fileService.getFilesByEntity(fileTargetType, entityId);
        
        return FlowXResponse.<List<FileResponse>>builder()
                .code(200)
                .message("Files retrieved successfully")
                .data(files)
                .build();
    }

    @Operation(
            summary = "Get my files",
            description = "Retrieves all files uploaded by the current user.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "My files retrieved successfully"
                    )
            }
    )
    @GetMapping("/my-files")
    public FlowXResponse<List<FileResponse>> getMyFiles() {
        log.info("Get my files request");
        List<FileResponse> files = fileService.getMyFiles();
        return FlowXResponse.<List<FileResponse>>builder()
                .code(200)
                .message("My files retrieved successfully")
                .data(files)
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
    @DeleteMapping("/batch")
    public FlowXResponse<Map<String, Object>> batchDeleteFiles(
            @Valid @RequestBody BatchDeleteRequest request) {

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
}
