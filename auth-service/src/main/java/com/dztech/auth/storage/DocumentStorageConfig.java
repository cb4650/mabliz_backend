package com.dztech.auth.storage;

import io.minio.MinioClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DocumentStorageProperties.class)
public class DocumentStorageConfig {

    @Bean
    public MinioClient minioClient(DocumentStorageProperties properties) {
        MinioClient.Builder builder = MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey());

        if (!properties.getRegion().isBlank()) {
            builder.region(properties.getRegion());
        }

        return builder.build();
    }

    @Bean
    public DocumentStorageService documentStorageService(
            MinioClient minioClient, DocumentStorageProperties properties) {
        return new MinioDocumentStorageService(minioClient, properties);
    }
}
