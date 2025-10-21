package com.dztech.auth.security;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

public final class JwtAuthenticatedUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Long userId;
    private final String username;
    private final String email;
    private final String phone;
    private final String name;
    private final Map<String, Object> claims;

    public JwtAuthenticatedUser(
            Long userId,
            String username,
            String email,
            String phone,
            String name,
            Map<String, Object> claims) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.name = name;
        this.claims = claims == null ? Map.of() : Collections.unmodifiableMap(claims);
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getName() {
        return name;
    }

    public Map<String, Object> getClaims() {
        return claims;
    }
}
