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
import project.ii.flowx.applications.service.helper.MinioService;
import project.ii.flowx.model.entity.File;
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

    // Cleanup failed uploads (chạy mỗi giờ)
    @Scheduled(fixedRate = 3600000)
    public void cleanupFailedUploads() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(1); // 1 giờ trước

        List<File> failedFiles = fileService.findByStatusAndCreatedAtBefore(FileStatus.FAILED, cutoff);

        for (File file : failedFiles) {
            try {
                // Xóa file trong MinIO nếu có
                if (minioService.objectExists(file.getObjectKey())) minioService.removeObject(file);
                // Xóa record khỏi database
                fileService.deleteFile(file.getId());
                // Send notification to the user

                log.info("Cleaned up failed upload: {}", file.getId());
            } catch (Exception e) {
                log.error("Error cleaning up file {}: {}", file.getId(), e.getMessage());
            }
        }
    }
}