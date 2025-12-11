package com.dztech.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class DocumentTokenService {

    public static final String SCOPE_PROFILE_DOCUMENT = "driver-doc";
    public static final String SCOPE_VEHICLE_DOCUMENT = "driver-vehicle-doc";

    private final SecretKey signingKey;
    private final JwtParser parser;
    private final Duration ttl;
    private final String issuer;

    public DocumentTokenService(
            @Value("${document.token.secret:}") String secret,
            @Value("${jwt.secret}") String fallbackSecret,
            @Value("${jwt.issuer}") String issuer,
            @Value("${document.token.ttl:PT10M}") Duration ttl) {
        String resolvedSecret = StringUtils.hasText(secret) ? secret.trim() : fallbackSecret;
        if (!StringUtils.hasText(resolvedSecret)) {
            throw new IllegalArgumentException("Document token secret must be configured");
        }
        byte[] secretBytes = resolvedSecret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalArgumentException("Document token secret must be at least 32 bytes long");
        }
        this.signingKey = Keys.hmacShaKeyFor(secretBytes);
        this.issuer = issuer;
        this.ttl = (ttl == null || ttl.isNegative() || ttl.isZero()) ? Duration.ofMinutes(10) : ttl;
        this.parser = Jwts.parserBuilder().setSigningKey(signingKey).requireIssuer(issuer).build();
    }

    public String issueProfileDocumentToken(Long driverId, String label) {
        Instant now = Instant.now();
        Instant expiry = now.plus(ttl);
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setIssuer(issuer)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .claim("driverId", driverId)
                .claim("label", label)
                .claim("scope", SCOPE_PROFILE_DOCUMENT)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String issueVehicleDocumentToken(Long driverId, Long vehicleId, String label) {
        Instant now = Instant.now();
        Instant expiry = now.plus(ttl);
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setIssuer(issuer)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .claim("driverId", driverId)
                .claim("vehicleId", vehicleId)
                .claim("label", label)
                .claim("scope", SCOPE_VEHICLE_DOCUMENT)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public DocumentTokenClaims parse(String token) {
        if (!StringUtils.hasText(token)) {
            throw new AccessDeniedException("Document token is required");
        }
        try {
            Claims claims = parser.parseClaimsJws(token).getBody();
            Long driverId = claims.get("driverId", Long.class);
            Long vehicleId = claims.get("vehicleId", Long.class);
            String label = claims.get("label", String.class);
            String scope = claims.get("scope", String.class);
            if (driverId == null || !StringUtils.hasText(label) || !StringUtils.hasText(scope)) {
                throw new AccessDeniedException("Invalid document token");
            }
            boolean validProfileScope = SCOPE_PROFILE_DOCUMENT.equals(scope);
            boolean validVehicleScope = SCOPE_VEHICLE_DOCUMENT.equals(scope);
            if (!validProfileScope && !validVehicleScope) {
                throw new AccessDeniedException("Invalid document token");
            }
            if (validVehicleScope && vehicleId == null) {
                throw new AccessDeniedException("Invalid document token");
            }
            return new DocumentTokenClaims(driverId, vehicleId, label, scope);
        } catch (ExpiredJwtException ex) {
            throw new AccessDeniedException("Document token expired");
        } catch (JwtException ex) {
            throw new AccessDeniedException("Invalid document token");
        }
    }

    public record DocumentTokenClaims(Long driverId, Long vehicleId, String label, String scope) {}
}
