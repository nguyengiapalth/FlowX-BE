package project.ii.flowx.applications.service.helper;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;
import project.ii.flowx.model.dto.file.PresignedResponse;
import project.ii.flowx.model.entity.File;

import java.security.MessageDigest;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MinioService {

    final MinioClient minioClient;

    @Value("${minio.bucket-name}") String bucketName;
    @Value("${minio.presigned.expiry:3600}") int presignedExpiry;

    @Transactional(readOnly = true)
    public PresignedResponse getPresignedUploadUrl(String fileName) {
        try {
            // Ensure bucket exists
            ensureBucketExists();

            // Generate unique object key
            String objectKey = generateObjectName(fileName);

            // Generate presigned URL for upload
            String presignedUrl = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(bucketName)
                            .object(objectKey)
                            .expiry(presignedExpiry, TimeUnit.SECONDS)
//                            .extraHeaders(contentType != null ?
//                                    Map.of("Content-Type", contentType) : null)
                            .build()
            );

            log.info("Generated presigned upload URL for file: {}", fileName);
            return PresignedResponse.builder()
                    .url(presignedUrl)
                    .bucket(bucketName)
                    .objectKey(objectKey)
                    .build();

        } catch (Exception e) {
            log.error("Error generating presigned upload URL: {}", e.getMessage());
            throw new FlowXException(FlowXError.INTERNAL_SERVER_ERROR,
                    "Failed to generate upload URL: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public String getPresignedDownloadUrl(File file, int presignedExpiryTime) {
        try {
            // Generate presigned URL for download
            String presignedUrl = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(file.getBucket())
                            .object(file.getObjectKey())
                            .expiry(Math.max(presignedExpiry, presignedExpiryTime), TimeUnit.SECONDS)
                            .build()
            );
            log.info("Generated presigned download URL for file: {}", file.getName());


            return presignedUrl;

        } catch (Exception e) {
            log.error("Error generating presigned download URL: {}", e.getMessage());
            throw new FlowXException(FlowXError.INTERNAL_SERVER_ERROR, "Failed to generate download URL: " + e.getMessage());
        }
    }

    public void removeObject(File file) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(file.getBucket())
                            .object(file.getObjectKey())
                            .build()
            );
            log.info("Removed file from MinIO: {}", file.getName());
        } catch (Exception e) {
            log.error("Error removing file from MinIO: {}", e.getMessage());
            throw new FlowXException(FlowXError.INTERNAL_SERVER_ERROR, "Failed to remove file: " + e.getMessage());
        }
    }

    // Get object info
    @Transactional(readOnly = true)
    public StatObjectResponse getObjectInfo(String objectPath) {
        try {
            return minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectPath)
                            .build()
            );
        } catch (Exception e) {
            log.error("Error getting object info: {}", e.getMessage());
            throw new FlowXException(FlowXError.INTERNAL_SERVER_ERROR,
                    "Failed to get object info: " + e.getMessage());
        }
    }

    public boolean objectExists(String objectPath) {
        log.info("Checking if object exists in MinIO: {}", objectPath);
        try {
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectPath)
                            .build()
            );
            return stat != null;
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                return false;
            }
            log.error("Error checking object existence: {}", e.getMessage());
            throw new FlowXException(FlowXError.INTERNAL_SERVER_ERROR,
                    "Failed to check object existence: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error checking object existence: {}", e.getMessage());
            throw new FlowXException(FlowXError.INTERNAL_SERVER_ERROR,
                    "Failed to check object existence: " + e.getMessage());
        }
    }

    private void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build()
                );
                log.info("Created bucket: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("Error ensuring bucket exists: {}", e.getMessage());
            throw new FlowXException(FlowXError.INTERNAL_SERVER_ERROR, "Failed to ensure bucket exists");
        }
    }

    private String generateObjectName(String originalFileName) {
        String uuid = java.util.UUID.randomUUID().toString();
        String timestamp = String.valueOf(Instant.now().toEpochMilli());
        String extension = "";

        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        return uuid + "_" + timestamp;  // + "_" + originalFileName;
    }


    private String calculateHash(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data);
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception e) {
            log.warn("Error calculating file hash: {}", e.getMessage());
            return null;
        }
    }
}

