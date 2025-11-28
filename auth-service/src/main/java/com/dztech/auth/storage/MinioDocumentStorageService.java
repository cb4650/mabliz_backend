package com.dztech.auth.storage;

import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinioDocumentStorageService implements DocumentStorageService {

    private static final Logger log = LoggerFactory.getLogger(MinioDocumentStorageService.class);

    private final MinioClient minioClient;
    private final DocumentStorageProperties properties;

    public MinioDocumentStorageService(MinioClient minioClient, DocumentStorageProperties properties) {
        this.minioClient = minioClient;
        this.properties = properties;
        ensureBucketExists();
    }

    @Override
    public String upload(String objectName, InputStream content, long size, String contentType) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(objectName)
                    .contentType(contentType)
                    .stream(content, size, -1)
                    .build());
            return objectName;
        } catch (Exception ex) {
            throw new DocumentStorageException("Failed to upload document to storage", ex);
        }
    }

    @Override
    public Optional<byte[]> download(String objectName) {
        try (InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(properties.getBucket())
                .object(objectName)
                .build())) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            stream.transferTo(buffer);
            return Optional.of(buffer.toByteArray());
        } catch (ErrorResponseException ex) {
            if (ex.errorResponse() != null && ex.errorResponse().code().equals("NoSuchKey")) {
                log.debug("Document not found in storage: {}", objectName);
                return Optional.empty();
            }
            throw new DocumentStorageException("Failed to download document from storage", ex);
        } catch (Exception ex) {
            throw new DocumentStorageException("Failed to download document from storage", ex);
        }
    }

    @Override
    public Optional<String> getPresignedUrl(String objectName) {
        try {
            String url = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(properties.getBucket())
                    .object(objectName)
                    .method(Method.GET)
                    .expiry((int) properties.getPresignedUrlExpiry().toSeconds())
                    .build());
            return Optional.of(url);
        } catch (ErrorResponseException ex) {
            if (ex.errorResponse() != null && ex.errorResponse().code().equals("NoSuchKey")) {
                log.debug("Document not found in storage: {}", objectName);
                return Optional.empty();
            }
            throw new DocumentStorageException("Failed to generate presigned URL", ex);
        } catch (Exception ex) {
            throw new DocumentStorageException("Failed to generate presigned URL", ex);
        }
    }

    private void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(properties.getBucket())
                    .build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(properties.getBucket())
                        .build());
            }
        } catch (Exception ex) {
            throw new DocumentStorageException("Failed to ensure document bucket exists", ex);
        }
    }
}
