package project.ii.flowx.config;

import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Minio client.
 * This class sets up the Minio client with the specified endpoint, access key, and secret key.
 * It uses the MinioClient builder to create an instance of MinioClient.
 */
@Configuration
@Slf4j
public class MinioConfig {
    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.useSSL:false}")
    private boolean useSSL;

    @Bean
    public MinioClient minioClient() {
        log.info("Configuring MinioClient with endpoint: {}", endpoint);
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
} 