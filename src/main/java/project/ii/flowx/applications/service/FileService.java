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
            String presignedUrl = minioService.getPresignedDownloadUrl(file, 3600);
            return presignedUrl;
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

            minioService.removeObject(file);
            fileRepository.delete(file);


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
