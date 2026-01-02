package com.dztech.auth.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Bean
    @ConditionalOnProperty(name = "firebase.enabled", havingValue = "true")
    public FirebaseApp firebaseApp(
            @Value("${firebase.credentials.json:}") String credentialsJson,
            @Value("${firebase.credentials.file:}") String credentialsFile,
            @Value("${firebase.project-id:}") String projectId)
            throws IOException {

        GoogleCredentials credentials = resolveCredentials(credentialsJson, credentialsFile);
        FirebaseOptions.Builder options = FirebaseOptions.builder().setCredentials(credentials);
        if (StringUtils.hasText(projectId)) {
            options.setProjectId(projectId.trim());
        }

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp app = FirebaseApp.initializeApp(options.build());
            log.info("Initialized FirebaseApp{}", StringUtils.hasText(projectId) ? " for project " + projectId : "");
            return app;
        }

        return FirebaseApp.getInstance();
    }

    @Bean
    @ConditionalOnBean(FirebaseApp.class)
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }

    private GoogleCredentials resolveCredentials(String credentialsJson, String credentialsFile) throws IOException {
        if (StringUtils.hasText(credentialsJson)) {
            try (InputStream inputStream =
                    new ByteArrayInputStream(credentialsJson.trim().getBytes(StandardCharsets.UTF_8))) {
                return GoogleCredentials.fromStream(inputStream);
            }
        }

        if (StringUtils.hasText(credentialsFile)) {
            try (InputStream inputStream = getCredentialsInputStream(credentialsFile.trim())) {
                return GoogleCredentials.fromStream(inputStream);
            }
        }

        throw new IllegalStateException(
                "Firebase credentials are required. Supply firebase.credentials.json or firebase.credentials.file");
    }

    private InputStream getCredentialsInputStream(String credentialsFile) throws IOException {
        if (credentialsFile.startsWith("classpath:")) {
            String resourcePath = credentialsFile.substring("classpath:".length());
            if (resourcePath.startsWith("/")) {
                resourcePath = resourcePath.substring(1);
            }

            ClassPathResource resource = new ClassPathResource(resourcePath);
            if (!resource.exists()) {
                throw new IllegalStateException("Firebase credentials file not found on classpath: " + resourcePath);
            }
            return resource.getInputStream();
        }

        return Files.newInputStream(Path.of(credentialsFile));
    }
}
