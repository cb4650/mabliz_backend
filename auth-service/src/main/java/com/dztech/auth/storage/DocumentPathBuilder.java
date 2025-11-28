package com.dztech.auth.storage;

import java.text.Normalizer;
import java.util.Locale;

public final class DocumentPathBuilder {

    private DocumentPathBuilder() {}

    public static String profileDocument(Long userId, String label) {
        return "drivers/%d/profile/%s".formatted(userId, sanitize(label));
    }

    public static String vehicleDocument(Long userId, String vehicleNumber, String label) {
        return "drivers/%d/vehicles/%s/%s".formatted(userId, sanitize(vehicleNumber), sanitize(label));
    }

    private static String sanitize(String value) {
        String normalized =
                Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
        String lower = normalized.toLowerCase(Locale.ROOT).trim();
        return lower.replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }
}
