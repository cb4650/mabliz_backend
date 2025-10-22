package com.dztech.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JwtTokenService {

    private static final String TOKEN_TYPE_CLAIM = "token_type";
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
            "name",
            TOKEN_TYPE_CLAIM);

    private final SecretKey signingKey;
    private final JwtParser jwtParser;
    private final Duration accessTokenTtl;
    private final Duration refreshTokenTtl;
    private final String issuer;

    public JwtTokenService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.issuer}") String issuer,
            @Value("${jwt.access-expiration-minutes:1440}") long accessExpirationMinutes,
            @Value("${jwt.refresh-expiration-minutes:43200}") long refreshExpirationMinutes) {
        if (!StringUtils.hasText(secret)) {
            throw new IllegalArgumentException("JWT secret must be configured");
        }
        byte[] secretBytes = secret.trim().getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 bytes long");
        }
        this.signingKey = Keys.hmacShaKeyFor(secretBytes);
        this.issuer = issuer;
        this.accessTokenTtl = Duration.ofMinutes(Math.max(1, accessExpirationMinutes));
        this.refreshTokenTtl = Duration.ofMinutes(Math.max(1, refreshExpirationMinutes));
        this.jwtParser = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .requireIssuer(issuer)
                .build();
    }

    public String generateAccessToken(JwtTokenPayload payload) {
        return generateToken(payload, TokenType.ACCESS, accessTokenTtl);
    }

    public String generateRefreshToken(JwtTokenPayload payload) {
        return generateToken(payload, TokenType.REFRESH, refreshTokenTtl);
    }

    private String generateToken(JwtTokenPayload payload, TokenType tokenType, Duration ttl) {
        Objects.requireNonNull(payload, "payload cannot be null");
        if (payload.userId() == null) {
            throw new IllegalArgumentException("User id is required to generate JWT");
        }
        Instant now = Instant.now();
        Instant expiry = now.plus(ttl);

        Map<String, Object> claims = new HashMap<>();
        if (StringUtils.hasText(payload.username())) {
            claims.put("username", payload.username());
        }
        if (StringUtils.hasText(payload.email())) {
            claims.put("email", payload.email());
        }
        if (StringUtils.hasText(payload.phone())) {
            claims.put("phone", payload.phone());
        }
        if (StringUtils.hasText(payload.name())) {
            claims.put("name", payload.name());
        }
        payload.additionalClaims().forEach((key, value) -> {
            if (!claims.containsKey(key)) {
                claims.put(key, value);
            }
        });
        claims.put(TOKEN_TYPE_CLAIM, tokenType.name());

        return Jwts.builder()
                .setSubject(payload.userId().toString())
                .setIssuer(issuer)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .addClaims(claims)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public JwtClaims parseAccessToken(String token) {
        JwtClaims claims = parseToken(token);
        ensureTokenType(claims.tokenType(), TokenType.ACCESS);
        return claims;
    }

    public JwtClaims parseRefreshToken(String token) {
        JwtClaims claims = parseToken(token);
        ensureTokenType(claims.tokenType(), TokenType.REFRESH);
        return claims;
    }

    private JwtClaims parseToken(String token) {
        try {
            Jws<Claims> jws = jwtParser.parseClaimsJws(token);
            Claims claims = jws.getBody();
            Long userId = parseUserId(claims.getSubject());
            String username = claims.get("username", String.class);
            String email = claims.get("email", String.class);
            String phone = claims.get("phone", String.class);
            String name = claims.get("name", String.class);
            TokenType tokenType = parseTokenType(claims.get(TOKEN_TYPE_CLAIM, String.class));

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
                    tokenType,
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

    private TokenType parseTokenType(String rawTokenType) {
        if (!StringUtils.hasText(rawTokenType)) {
            throw new JwtAuthenticationException("JWT token type is missing");
        }
        try {
            return TokenType.valueOf(rawTokenType);
        } catch (IllegalArgumentException ex) {
            throw new JwtAuthenticationException("JWT token type is invalid", ex);
        }
    }

    private void ensureTokenType(TokenType actual, TokenType expected) {
        if (actual != expected) {
            throw new JwtAuthenticationException("JWT token is not a " + expected.name().toLowerCase() + " token");
        }
    }

    public record JwtTokenPayload(
            Long userId,
            String username,
            String email,
            String phone,
            String name,
            Map<String, Object> additionalClaims) {

        public JwtTokenPayload {
            additionalClaims = additionalClaims == null ? Map.of() : Map.copyOf(additionalClaims);
        }
    }

    public record JwtClaims(
            Long userId,
            String username,
            String email,
            String phone,
            String name,
            TokenType tokenType,
            Map<String, Object> additionalClaims) {
    }

    public enum TokenType {
        ACCESS,
        REFRESH
    }
}
