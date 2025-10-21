package com.dztech.rayder.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JwtTokenService {

    private static final Set<String> RESERVED_CLAIMS = Set.of(
            Claims.SUBJECT,
            Claims.ISSUER,
            Claims.AUDIENCE,
            Claims.EXPIRATION,
            Claims.NOT_BEFORE,
            Claims.ISSUED_AT,
            Claims.ID,
            "username",
            "email",
            "phone",
            "name");

    private final SecretKey signingKey;
    private final JwtParser jwtParser;

    public JwtTokenService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.issuer}") String issuer) {
        if (!StringUtils.hasText(secret)) {
            throw new IllegalArgumentException("JWT secret must be configured");
        }
        byte[] secretBytes = secret.trim().getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 bytes long");
        }
        this.signingKey = Keys.hmacShaKeyFor(secretBytes);
        this.jwtParser = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .requireIssuer(issuer)
                .build();
    }

    public JwtClaims parseToken(String token) {
        try {
            Jws<Claims> jws = jwtParser.parseClaimsJws(token);
            Claims claims = jws.getBody();
            Long userId = parseUserId(claims.getSubject());
            String username = claims.get("username", String.class);
            String email = claims.get("email", String.class);
            String phone = claims.get("phone", String.class);
            String name = claims.get("name", String.class);

            Map<String, Object> additional = new HashMap<>();
            claims.forEach((key, value) -> {
                if (!RESERVED_CLAIMS.contains(key)) {
                    additional.put(key, value);
                }
            });

            return new JwtClaims(
                    userId,
                    username,
                    email,
                    phone,
                    name,
                    Collections.unmodifiableMap(additional));
        } catch (UnsupportedJwtException ex) {
            throw ex;
        } catch (ExpiredJwtException ex) {
            throw new JwtAuthenticationException("JWT token has expired", ex);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new JwtAuthenticationException("Invalid JWT token", ex);
        }
    }

    private Long parseUserId(String subject) {
        if (!StringUtils.hasText(subject)) {
            throw new JwtAuthenticationException("JWT subject (user id) is missing");
        }
        try {
            return Long.valueOf(subject);
        } catch (NumberFormatException ex) {
            throw new JwtAuthenticationException("JWT subject must be numeric", ex);
        }
    }

    public record JwtClaims(
            Long userId,
            String username,
            String email,
            String phone,
            String name,
            Map<String, Object> additionalClaims) {
    }
}
