package com.dztech.auth.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class DriverDocumentUrlBuilder {

    private final String publicApiPrefix;

    public DriverDocumentUrlBuilder(@Value("${app.public-api-prefix:}") String publicApiPrefix) {
        this.publicApiPrefix = normalizePrefix(publicApiPrefix);
    }

    public String profileDocument(Long driverId, String label, String token) {
        String base = "%s/api/driver/profile/documents/%d/%s".formatted(publicApiPrefix, driverId, label);
        if (!StringUtils.hasText(token)) {
            return base;
        }
        return base + "?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
    }

    public String vehicleDocument(Long vehicleId, String label, String token) {
        String base = "%s/api/driver/vehicles/documents/%d/%s".formatted(publicApiPrefix, vehicleId, label);
        if (!StringUtils.hasText(token)) {
            return base;
        }
        return base + "?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
    }

    private String normalizePrefix(String raw) {
        if (!StringUtils.hasText(raw)) {
            return "";
        }
        String trimmed = raw.trim();
        String ensuredLeading = trimmed.startsWith("/") ? trimmed : "/" + trimmed;
        return ensuredLeading.endsWith("/") ? ensuredLeading.substring(0, ensuredLeading.length() - 1) : ensuredLeading;
    }
}
