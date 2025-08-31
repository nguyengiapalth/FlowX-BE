package project.ii.flowx.module.file;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ii.flowx.applications.helper.EntityLookupService;
import project.ii.flowx.applications.helper.MinioService;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;
import project.ii.flowx.module.file.dto.FileCreateRequest;
import project.ii.flowx.module.file.dto.FileResponse;
import project.ii.flowx.module.file.dto.PresignedResponse;
import project.ii.flowx.module.file.dto.PresignedUploadResponse;
import project.ii.flowx.module.file.entity.File;
import project.ii.flowx.module.user.entity.User;
import project.ii.flowx.security.UserPrincipal;
import project.ii.flowx.applications.enums.FileTargetType;
import project.ii.flowx.applications.enums.FileStatus;
import io.minio.StatObjectResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileService {
    FileRepository fileRepository;
    FileMapper fileMapper;
    MinioService minioService;
    EntityLookupService entityLookupService;
    ApplicationEventPublisher eventPublisher;

    public PresignedUploadResponse getPresignedUploadUrl(FileCreateRequest createRequest) {
        try {
            // get user form security context
            User user = getCurrentUser();

            PresignedResponse presignedUrl = minioService.getPresignedUploadUrl(createRequest.getName());

            File file = fileMapper.toFile(createRequest);
            file.setUploaderId(user.getId());
            file.setFileStatus(FileStatus.PROCESSING);
            file.setObjectKey(presignedUrl.getObjectKey());
            file.setBucket(presignedUrl.getBucket());
            fileRepository.save(file);

            return PresignedUploadResponse.builder()
                    .url(presignedUrl.getUrl())
                    .presignedFileId(file.getId())
                    .build();
        } catch (Exception e) {
            throw new FlowXException(FlowXError.INTERNAL_SERVER_ERROR,
                    "Failed to generate upload URL: " + e.getMessage());
        }
    }

    @Transactional
    public void deleteFile(UUID fileId) {
        try {
            File file = getFileById(fileId);

            minioService.removeObject(file);
            fileRepository.delete(file);

            log.info("File deleted and event published: {}", fileId);
        } catch (Exception e) {
            throw new FlowXException(FlowXError.INTERNAL_SERVER_ERROR,
                    "Failed to delete file: " + e.getMessage());
        }
    }

    @Transactional
    public void deleteFilesByTarget(FileTargetType fileTargetType, UUID targetId) {
        List<File> files = fileRepository.findByTargetIdAndFileTargetType(targetId, fileTargetType);
        if (files.isEmpty()) {
            log.warn("No files found for target {} with type {}", targetId, fileTargetType);
            return;
        }

        for (File file : files) {
            try {
                minioService.removeObject(file);
                fileRepository.delete(file);
                log.info("Deleted file: {}", file.getId());
            } catch (Exception e) {
                log.error("Failed to delete file {}: {}", file.getId(), e.getMessage());
            }
        }
    }

    @Transactional(readOnly = true)
    public List<FileResponse> getFilesByTarget(FileTargetType fileTargetType, UUID targetId) {
        List<File> files = fileRepository.findByTargetIdAndFileTargetType(targetId, fileTargetType);
        return fileMapper.toFileResponseList(files);
    }

    /**
     * Find files by status and created before a certain date
     * Used for cleanup jobs to find files that failed to upload
     * No implement caching here as this is a cleanup operation
     */
    @Transactional(readOnly = true)
    public List<File> findByStatusAndCreatedAtBefore(FileStatus fileStatus, LocalDateTime cutoff) {
        if (fileStatus == null || cutoff == null) {
            throw new FlowXException(FlowXError.BAD_REQUEST, "File status and cutoff date must be provided");
        }
        return fileRepository.findByFileStatusAndCreatedAtBefore(fileStatus, cutoff);
    }

    /**
     * Method to be called from FE to confirm file upload success
     */
    @Transactional
    public void confirmFileUpload(UUID fileId) {
        File file = getFileById(fileId);
        
        // Validate that file is in PROCESSING status
        if (file.getFileStatus() != FileStatus.PROCESSING) {
            throw new FlowXException(FlowXError.BAD_REQUEST, 
                "File is not in processing status. Current status: " + file.getFileStatus());
        }
        
        try {
            // Check if file exists in MinIO
            if (!minioService.objectExists(file.getObjectKey())) {
                throw new FlowXException(FlowXError.BAD_REQUEST, 
                    "File not found in storage. Upload may have failed.");
            }
            
            // Get actual file size from MinIO
            StatObjectResponse objectInfo = minioService.getObjectInfo(file.getObjectKey());
            file.setActualSize(objectInfo.size());
            
            // Mark file as uploaded
            markFileAsUploaded(file);
            
            log.info("File upload confirmed by FE: {}", file.getId());
            
        } catch (Exception e) {
            // If any error occurs, mark file as failed
            file.setFileStatus(FileStatus.FAILED);
            fileRepository.save(file);
            
            log.error("File upload confirmation failed for file {}: {}", file.getId(), e.getMessage());
            throw new FlowXException(FlowXError.BAD_REQUEST, 
                "File upload confirmation failed: " + e.getMessage());
        }
    }

    @Transactional
    public void markFileAsUploaded(File file) {
        file.setFileStatus(FileStatus.UPLOADED);
        fileRepository.save(file);

        // Publish file uploaded event to trigger hasFile flag sync
        UUID uploaderId = file.getUploaderId() != null ? file.getUploaderId() : null;
        log.info("File upload completed and event published: {}", file.getId());
    }

    // helper method
    private File getFileById(UUID fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "File not found"));
    }

    private User getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new FlowXException(FlowXError.UNAUTHENTICATED, "User not authenticated");
        }
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        UUID userId = userPrincipal.getId();
        return entityLookupService.getUserById(userId);
    }
}
