package com.dztech.rayder.client;

import com.dztech.rayder.dto.TripConfirmedNotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class InternalNotificationClient {

    private static final Logger log = LoggerFactory.getLogger(InternalNotificationClient.class);
    private static final String INTERNAL_API_KEY_HEADER = "X-Internal-Api-Key";

    private final RestTemplate restTemplate;
    private final String authServiceBaseUrl;
    private final String internalApiKey;

    public InternalNotificationClient(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${auth.service.base-url:http://localhost:8081}") String authServiceBaseUrl,
            @Value("${internal.api.key:}") String internalApiKey) {
        this.restTemplate = restTemplateBuilder.build();
        this.authServiceBaseUrl = authServiceBaseUrl;
        this.internalApiKey = internalApiKey;
    }

    public void sendTripConfirmed(TripConfirmedNotificationRequest request) {
        if (!StringUtils.hasText(internalApiKey)) {
            log.warn("Internal API key is not configured; skipping push notification call");
            return;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add(INTERNAL_API_KEY_HEADER, internalApiKey);
            HttpEntity<TripConfirmedNotificationRequest> entity = new HttpEntity<>(request, headers);

            String url = authServiceBaseUrl + "/api/internal/notifications/trip-confirmed";
            restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
        } catch (RestClientException ex) {
            log.error("Failed to notify auth-service about trip confirmation", ex);
        }
    }
}
