package project.ii.flowx.applications.helper;

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
import project.ii.flowx.module.file.dto.PresignedResponse;
import project.ii.flowx.module.file.entity.File;

import java.security.MessageDigest;
import java.time.LocalDateTime;
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
            ensureBucketExists();
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

            return PresignedResponse.builder()
                    .url(presignedUrl)
                    .bucket(bucketName)
                    .objectKey(objectKey)
                    .build();

        } catch (Exception e) {
            throw new FlowXException(FlowXError.INTERNAL_SERVER_ERROR,
                    "Failed to generate upload URL: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public String getPresignedDownloadUrl(File file, int presignedExpiryTime){
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(file.getBucket())
                            .object(file.getObjectKey())
                            .expiry(Math.max(presignedExpiry, presignedExpiryTime), TimeUnit.SECONDS)
                            .build()
            );
        } catch (Exception e) {
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
        } catch (Exception e) {
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
            throw new FlowXException(FlowXError.INTERNAL_SERVER_ERROR,
                    "Failed to get object info: " + e.getMessage());
        }
    }

    public boolean objectExists(String objectPath) {
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
            throw new FlowXException(FlowXError.INTERNAL_SERVER_ERROR,
                    "Failed to check object existence: " + e.getMessage());
        } catch (Exception e) {
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
            }
        } catch (Exception e) {
            throw new FlowXException(FlowXError.INTERNAL_SERVER_ERROR, "Failed to ensure bucket exists");
        }
    }

    private String generateObjectName(String originalFileName) {
        String uuid = java.util.UUID.randomUUID().toString();
        String timestamp = String.valueOf(LocalDateTime.now().toEpochSecond(java.time.ZoneOffset.UTC) * 1000);
        String extension = "";

        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        return uuid + "_" + timestamp + extension;
    }

    public String getPresignedDownloadUrlFromObjectKey(String objectKey, int presignedExpiryTime) {
        if (objectKey == null || objectKey.trim().isEmpty()) {
            return null;
        }
        
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectKey)
                            .expiry(Math.max(presignedExpiry, presignedExpiryTime), TimeUnit.SECONDS)
                            .build()
            );

        } catch (Exception e) {
            return null;
        }
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
            return null;
        }
    }
}

