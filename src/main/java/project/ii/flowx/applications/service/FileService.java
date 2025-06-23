package project.ii.flowx.applications.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ii.flowx.applications.service.helper.EntityLookupService;
import project.ii.flowx.applications.service.helper.MinioService;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;
import project.ii.flowx.model.dto.file.FileCreateRequest;
import project.ii.flowx.model.dto.file.FileResponse;
import project.ii.flowx.model.dto.file.PresignedResponse;
import project.ii.flowx.model.dto.file.PresignedUploadResponse;
import project.ii.flowx.model.entity.File;
import project.ii.flowx.model.entity.User;
import project.ii.flowx.model.mapper.FileMapper;
import project.ii.flowx.model.repository.FileRepository;
import project.ii.flowx.security.UserPrincipal;
import project.ii.flowx.shared.enums.FileTargetType;
import project.ii.flowx.shared.enums.FileStatus;
import project.ii.flowx.applications.events.FileEvent;
import io.minio.StatObjectResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

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
//    CacheManager cacheManager;

    public PresignedUploadResponse getPresignedUploadUrl(FileCreateRequest createRequest) {
        try {
            // get user form security context
            Long userId = getCurrentUserId();
            User user = entityLookupService.getUserById(userId);

            PresignedResponse presignedUrl = minioService.getPresignedUploadUrl(createRequest.getName());

            File file = fileMapper.toFile(createRequest);
            file.setUploader(user);
            file.setFileStatus(FileStatus.PROCESSING);
            file.setObjectKey(presignedUrl.getObjectKey());
            file.setBucket(presignedUrl.getBucket());
            fileRepository.save(file);

            // Clear cache for files associated with the entity (new file will be added)
//            String cacheKey = createRequest.getFileTargetType() + "-" + createRequest.getTargetId();
//            Objects.requireNonNull(cacheManager.getCache(cacheKey)).evict(cacheKey);
//            log.debug("Evicted files cache for key: {} due to new file upload", cacheKey);

            return PresignedUploadResponse.builder()
                    .url(presignedUrl.getUrl())
                    .presignedFileId(file.getId())
                    .objectKey(file.getObjectKey())
                    .build();
        } catch (Exception e) {
            throw new FlowXException(FlowXError.INTERNAL_SERVER_ERROR,
                    "Failed to generate upload URL: " + e.getMessage());
        }
    }

//    public String getPresignedDownloadUrl(Long fileId) {
//        File file = getFileById(fileId);
//        try {
//            // Generate presigned URL for download
//            return minioService.getPresignedDownloadUrl(file, 3600);
//        } catch (Exception e) {
//            throw new FlowXException(FlowXError.INTERNAL_SERVER_ERROR,
//                    "Failed to generate download URL: " + e.getMessage());
//        }
//    }

    @Transactional
    public void deleteFile(Long fileId) {
        try {
            File file = getFileById(fileId);
            Long uploaderId = file.getUploader() != null ? file.getUploader().getId() : null;
            String fileName = file.getName();
            Long entityId = file.getTargetId();
            String entityType = file.getFileTargetType().toString();

            minioService.removeObject(file);
            fileRepository.delete(file);
            
            // Clear cache for files associated with the entity
//            String cacheKey = file.getFileTargetType() + "-" + entityId;
//            Objects.requireNonNull(cacheManager.getCache("files")).evict(cacheKey);
//            log.debug("Evicted files cache for key: {}", cacheKey);

            // Publish file deleted event to trigger hasFile flag sync
            eventPublisher.publishEvent(new FileEvent.FileDeletedEvent(
                    fileId, 
                    fileName, 
                    uploaderId,
                    entityId,
                    entityType,
                    file.getObjectKey()
            ));

            log.info("File deleted and event published: {}", fileId);
        } catch (Exception e) {
            throw new FlowXException(FlowXError.INTERNAL_SERVER_ERROR,
                    "Failed to delete file: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
//    @Cacheable(value = "files", key = "#fileTargetType + '-' + #entityId",
//               unless = "#result == null || #result.isEmpty()")
    public List<FileResponse> getFilesByEntity(FileTargetType fileTargetType, Long entityId) {
        List<File> files = fileRepository.findByTargetIdAndFileTargetType(entityId, fileTargetType);

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
     * Method to be called when a file upload is completed (called from FileUploadCleanupJob)
     */
    @Transactional
    public void markFileAsUploaded(File file) {
        file.setFileStatus(FileStatus.UPLOADED);
        fileRepository.save(file);
        
        // Publish file uploaded event to trigger hasFile flag sync
        Long uploaderId = file.getUploader() != null ? file.getUploader().getId() : null;
        eventPublisher.publishEvent(new FileEvent.FileUploadedEvent(
                file.getId(),
                file.getName(),
                uploaderId,
                file.getTargetId(),
                file.getFileTargetType().toString(),
                file.getObjectKey()
        ));
        
        log.info("File upload completed and event published: {}", file.getId());
    }

    /**
     * Method to be called from FE to confirm file upload success
     */
    @Transactional
    public void confirmFileUpload(Long fileId) {
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

    // helper method
    private File getFileById(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "File not found"));
    }

    private Long getCurrentUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new FlowXException(FlowXError.UNAUTHENTICATED, "User not authenticated");
        }
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userPrincipal.getId();
    }
}
