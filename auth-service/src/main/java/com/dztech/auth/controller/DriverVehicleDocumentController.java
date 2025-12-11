package com.dztech.auth.controller;

import com.dztech.auth.security.DocumentTokenService;
import com.dztech.auth.security.JwtAuthenticatedUser;
import com.dztech.auth.security.JwtAuthenticationToken;
import com.dztech.auth.service.DriverVehicleDocumentAccessService;
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
@RequestMapping("/api/driver/vehicles/documents")
public class DriverVehicleDocumentController {

    private final DriverVehicleDocumentAccessService driverVehicleDocumentAccessService;
    private final DocumentTokenService documentTokenService;

    public DriverVehicleDocumentController(
            DriverVehicleDocumentAccessService driverVehicleDocumentAccessService,
            DocumentTokenService documentTokenService) {
        this.driverVehicleDocumentAccessService = driverVehicleDocumentAccessService;
        this.documentTokenService = documentTokenService;
    }

    @GetMapping("/{vehicleId}/{label}")
    public ResponseEntity<byte[]> getVehicleDocument(
            Authentication authentication,
            @PathVariable Long vehicleId,
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
            if (!DocumentTokenService.SCOPE_VEHICLE_DOCUMENT.equals(claims.scope())
                    || !vehicleId.equals(claims.vehicleId())
                    || !label.equalsIgnoreCase(claims.label())) {
                throw new AccessDeniedException("Invalid document token");
            }
            requesterId = claims.driverId();
            isAdmin = false;
        } else if (requesterId == null) {
            throw new AccessDeniedException("Authentication is required");
        }

        DriverVehicleDocumentAccessService.DriverDocumentResource document =
                driverVehicleDocumentAccessService.getVehicleDocument(requesterId, isAdmin, vehicleId, label);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(document.contentType()));
        if (!document.inlineImage()) {
            headers.setContentDisposition(ContentDisposition.attachment().filename(document.label()).build());
        }

        return new ResponseEntity<>(document.data(), headers, HttpStatus.OK);
    }
}
