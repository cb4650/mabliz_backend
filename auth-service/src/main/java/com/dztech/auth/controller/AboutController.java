package com.dztech.auth.controller;

import com.dztech.auth.dto.AboutRequest;
import com.dztech.auth.dto.AboutResponse;
import com.dztech.auth.service.AboutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/about")
@RequiredArgsConstructor
public class AboutController {

    private final AboutService aboutService;

    @GetMapping
    public ResponseEntity<AboutResponse> getAbout(@RequestHeader("appId") String appId) {
        validateAppId(appId);
        AboutResponse about = aboutService.getAboutByAppId(appId);
        return ResponseEntity.ok(about);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AboutResponse> createAbout(@Valid @RequestBody AboutRequest request) {
        AboutResponse about = aboutService.createAbout(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(about);
    }

    @PutMapping("/{appId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AboutResponse> updateAbout(
            @PathVariable String appId,
            @Valid @RequestBody AboutRequest request) {
        AboutResponse about = aboutService.updateAbout(appId, request);
        return ResponseEntity.ok(about);
    }

    @DeleteMapping("/{appId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAbout(@PathVariable String appId) {
        aboutService.deleteAbout(appId);
        return ResponseEntity.noContent().build();
    }

    private void validateAppId(String appId) {
        if (!"rydd".equals(appId) && !"rydc".equals(appId)) {
            throw new IllegalArgumentException("Invalid app ID. Must be 'rydd' or 'rydc'");
        }
    }
}
