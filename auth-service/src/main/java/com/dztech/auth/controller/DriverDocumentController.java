package com.dztech.auth.controller;

import com.dztech.auth.security.DocumentTokenService;
import com.dztech.auth.security.JwtAuthenticatedUser;
import com.dztech.auth.security.JwtAuthenticationToken;
import com.dztech.auth.service.DriverDocumentAccessService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/driver/profile/documents")
public class DriverDocumentController {

    private final DriverDocumentAccessService driverDocumentAccessService;
    private final DocumentTokenService documentTokenService;

    public DriverDocumentController(
            DriverDocumentAccessService driverDocumentAccessService, DocumentTokenService documentTokenService) {
        this.driverDocumentAccessService = driverDocumentAccessService;
        this.documentTokenService = documentTokenService;
    }

    @GetMapping("/{driverId}/{label}")
    public ResponseEntity<byte[]> getProfileDocument(
            Authentication authentication,
            @PathVariable Long driverId,
            @PathVariable String label,
            @RequestParam(value = "token", required = false) String token) {
        Long requesterId = null;
        boolean isAdmin = false;

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            JwtAuthenticatedUser principal = jwtAuth.getPrincipal();
            if (principal != null) {
                requesterId = principal.getUserId();
            }
            isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> "ROLE_ADMIN".equalsIgnoreCase(auth.getAuthority()));
        }

        if (StringUtils.hasText(token)) {
            DocumentTokenService.DocumentTokenClaims claims = documentTokenService.parse(token);
            if (!driverId.equals(claims.driverId()) || !label.equalsIgnoreCase(claims.label())) {
                throw new AccessDeniedException("Invalid document token");
            }
            requesterId = claims.driverId();
            isAdmin = false;
        } else if (requesterId == null) {
            throw new AccessDeniedException("Authentication is required");
        }

        DriverDocumentAccessService.DriverDocumentResource document = driverDocumentAccessService.getProfileDocument(
                requesterId, isAdmin, driverId, label);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(document.contentType()));
        if (!document.inlineImage()) {
            headers.setContentDisposition(ContentDisposition.attachment().filename(document.label()).build());
        }

        return new ResponseEntity<>(document.data(), headers, HttpStatus.OK);
    }
}
