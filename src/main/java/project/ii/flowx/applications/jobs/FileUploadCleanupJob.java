package project.ii.flowx.applications.jobs;

import io.minio.StatObjectResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import project.ii.flowx.applications.service.FileService;
import project.ii.flowx.applications.service.communicate.NotificationService;
import project.ii.flowx.applications.service.helper.MinioService;
import project.ii.flowx.model.dto.notification.NotificationCreateRequest;
import project.ii.flowx.model.entity.File;
import project.ii.flowx.model.entity.Notification;
import project.ii.flowx.shared.enums.FileStatus;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@EnableScheduling
public class FileUploadCleanupJob {

    FileService fileService;
    MinioService minioService;
    NotificationService notificationService;

    // Cleanup failed uploads (every hour)
    @Scheduled(fixedRate = 3600000)
    public void cleanupFailedUploads() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(1); // 1 hour ago

        List<File> failedFiles = fileService.findByStatusAndCreatedAtBefore(FileStatus.FAILED, cutoff);

        for (File file : failedFiles) {
            try {
                // Check if the file exists in MinIO
                if (minioService.objectExists(file.getObjectKey())) minioService.removeObject(file);
                // Delete the file record from the database
                fileService.deleteFile(file.getId());
                // Send notification to the user
                NotificationCreateRequest notificationCreateRequest = NotificationCreateRequest.builder()
                        .userId(file.getUploader().getId())
                        .title("File Upload Failed")
                        .content("Your file upload failed and has been cleaned up: " + file.getName())
                        .entityType(file.getFileTargetType().toString())
                        .entityId(file.getTargetId())
                        .build();

                notificationService.createNotification(notificationCreateRequest);

                log.info("Cleaned up failed upload: {}", file.getId());
            } catch (Exception e) {
                log.error("Error cleaning up file {}: {}", file.getId(), e.getMessage());
            }
        }
    }
}