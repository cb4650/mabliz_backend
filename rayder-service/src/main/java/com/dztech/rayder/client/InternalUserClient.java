package com.dztech.rayder.client;

import com.dztech.rayder.dto.InternalDriverProfileResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class InternalUserClient {

    private static final Logger log = LoggerFactory.getLogger(InternalUserClient.class);
    private static final String INTERNAL_API_KEY_HEADER = "X-Internal-Api-Key";

    private final RestTemplate restTemplate;
    private final String authServiceUrl;
    private final String internalApiKey;

    public InternalUserClient(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${auth.service.base-url:http://dztech-auth-service:8081}") String authServiceUrl,
            @Value("${internal.api.key:}") String internalApiKey) {
        this.restTemplate = restTemplateBuilder.build();
        this.authServiceUrl = authServiceUrl;
        this.internalApiKey = internalApiKey;
    }

    public InternalDriverProfileResponse getUserProfile(Long userId) {
        try {
            String url = authServiceUrl + "/api/internal/notifications/users/" + userId;
            log.info("Fetching user profile from: {}", url);

            HttpHeaders headers = new HttpHeaders();
            headers.set(INTERNAL_API_KEY_HEADER, internalApiKey);

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<InternalDriverProfileResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, InternalDriverProfileResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Successfully retrieved user profile for userId: {}", userId);
                return response.getBody();
            } else {
                log.warn("Failed to retrieve user profile for userId: {}, status: {}", userId, response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            log.error("Error retrieving user profile for userId: {}", userId, e);
            return null;
        }
    }
}
