package com.dztech.auth.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class RayderVehicleClient {

    private static final Logger log = LoggerFactory.getLogger(RayderVehicleClient.class);

    private final RestClient restClient;

    public RayderVehicleClient(
            @Value("${rayder.service.base-url:http://localhost:8082}") String rayderBaseUrl,
            RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl(rayderBaseUrl).build();
    }

    public long fetchVehicleCount(String accessToken) {
        if (!StringUtils.hasText(accessToken)) {
            return 0L;
        }

        try {
            VehicleCountResponse response = restClient.get()
                    .uri("/internal/vehicles/count")
                    .header(HttpHeaders.AUTHORIZATION, formatBearer(accessToken))
                    .retrieve()
                    .body(VehicleCountResponse.class);
            if (response == null) {
                return 0L;
            }
            return response.count();
        } catch (RestClientException ex) {
            log.warn("Failed to fetch vehicle count from rayder-service: {}", ex.getMessage());
            return 0L;
        }
    }

    private String formatBearer(String token) {
        return token.regionMatches(true, 0, "Bearer ", 0, 7) ? token : "Bearer " + token;
    }

    private record VehicleCountResponse(boolean success, long count) {
    }
}
