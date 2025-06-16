package project.ii.flowx.applications.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
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
            Long userId = getCurrentUserId();
            User user = entityLookupService.getUserById(userId);

            PresignedResponse presignedUrl = minioService.getPresignedUploadUrl(createRequest.getName());

            File file = fileMapper.toFile(createRequest);
            file.setUploader(user);
            file.setFileStatus(FileStatus.PROCESSING);
            file.setObjectKey(presignedUrl.getObjectKey());
            file.setBucket(presignedUrl.getBucket());
            fileRepository.save(file);

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

    public String getPresignedDownloadUrl(Long fileId) {
        File file = getFileById(fileId);
        try {
            // Generate presigned URL for download
            return minioService.getPresignedDownloadUrl(file, 3600);
        } catch (Exception e) {
            throw new FlowXException(FlowXError.INTERNAL_SERVER_ERROR,
                    "Failed to generate download URL: " + e.getMessage());
        }
    }

    @Transactional
    public void update(File file) {
        fileRepository.save(file);
    }

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
    public FileResponse getFileInfo(Long fileId) {
        File file = getFileById(fileId);
        return fileMapper.toFileResponse(file);
    }

    @Transactional(readOnly = true)
    public List<FileResponse> getFilesByEntity(FileTargetType fileTargetType, Long entityId) {
        List<File> files = fileRepository.findByTargetIdAndFileTargetType(entityId, fileTargetType);
        return fileMapper.toFileResponseList(files);
    }

    @Transactional(readOnly = true)
    public List<FileResponse> getMyFiles() {
        Long userId = getCurrentUserId();
        List<File> files = fileRepository.findByUploaderId(userId);
        List<FileResponse> fileResponses = fileMapper.toFileResponseList(files);
        // if type is image, video, .., set thumbnailUrl
        for (FileResponse fileResponse : fileResponses) {
            if (fileResponse.getType().startsWith("image/") || fileResponse.getType().startsWith("video/")) {
                // get file by id
                File file = files.stream()
                        .filter(f -> f.getId().equals(fileResponse.getId()))
                        .findFirst()
                        .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "File not found"));
                String url = minioService.getPresignedDownloadUrl(file, 3600 * 24); // 1 day expiry
                fileResponse.setUrl(url);
            }
        }
        return fileResponses;
    }

    @Transactional(readOnly = true)
    public List<File> findFilesByStatus(FileStatus status) {
        return fileRepository.findByFileStatus(status);
    }

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

    // heper method
    private File getFileById(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new FlowXException(FlowXError.NOT_FOUND, "File not found"));
    }

    private Long getCurrentUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new FlowXException(FlowXError.UNAUTHORIZED, "User not authenticated");
        }
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userPrincipal.getId();
    }
}
