package com.dztech.auth.storage;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "document.storage")
public class DocumentStorageProperties {

    /**
     * Endpoint for the S3-compatible object store. Use the MinIO URL locally and replace with an S3 URL later.
     */
    private final String endpoint;

    private final String accessKey;
    private final String secretKey;
    private final String bucket;
    private final String region;
    private final Duration presignedUrlExpiry;

    public DocumentStorageProperties(
            String endpoint,
            String accessKey,
            String secretKey,
            String bucket,
            @DefaultValue("") String region,
            @DefaultValue("PT1H") Duration presignedUrlExpiry) {
        this.endpoint = endpoint;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.bucket = bucket;
        this.region = region;
        this.presignedUrlExpiry = presignedUrlExpiry;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getBucket() {
        return bucket;
    }

    public String getRegion() {
        return region;
    }

    public Duration getPresignedUrlExpiry() {
        return presignedUrlExpiry;
    }
}
