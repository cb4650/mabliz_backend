package com.dztech.auth.storage;

import java.io.InputStream;
import java.time.Duration;
import java.util.Optional;

public interface DocumentStorageService {

    /**
     * Uploads a document and returns the object name that was persisted.
     */
    String upload(String objectName, InputStream content, long size, String contentType);

    /**
     * Downloads the stored document if present.
     */
    Optional<byte[]> download(String objectName);

    /**
     * Generates a presigned download URL if the document exists.
     */
    Optional<String> getPresignedUrl(String objectName);
}
