package com.dztech.auth.model;

import java.util.Arrays;

public enum AppId {
    ALL("all"),
    RYDC("rydc"),
    RYDD("rydd");

    private final String headerValue;

    AppId(String headerValue) {
        this.headerValue = headerValue;
    }

    public String value() {
        return headerValue;
    }

    public static AppId fromHeader(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("appId header is required");
        }
        String normalized = raw.trim().toLowerCase();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("appId header is required");
        }
        return Arrays.stream(values())
                .filter(appId -> appId.headerValue.equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid appId header. Supported values are: all, rydc, rydd"));
    }
}

