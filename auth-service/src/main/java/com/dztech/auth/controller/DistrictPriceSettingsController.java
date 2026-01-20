package com.dztech.auth.controller;

import com.dztech.auth.dto.DistrictPriceSettingsRequest;
import com.dztech.auth.dto.DistrictPriceSettingsResponse;
import com.dztech.auth.dto.FallbackPriceSettingsRequest;
import com.dztech.auth.model.FallbackPriceSettings;
import com.dztech.auth.service.DistrictPriceSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/district-price-settings")
@RequiredArgsConstructor
public class DistrictPriceSettingsController {

    private final DistrictPriceSettingsService districtPriceSettingsService;

    @GetMapping
    public ResponseEntity<List<DistrictPriceSettingsResponse>> getAllDistrictPriceSettings() {
        List<DistrictPriceSettingsResponse> response = districtPriceSettingsService.getAllDistrictPriceSettings();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/district/{districtId}")
    public ResponseEntity<List<DistrictPriceSettingsResponse>> getDistrictPriceSettings(@PathVariable Long districtId) {
        List<DistrictPriceSettingsResponse> response = districtPriceSettingsService.getDistrictPriceSettings(districtId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<DistrictPriceSettingsResponse> createDistrictPriceSettings(
            @Valid @RequestBody DistrictPriceSettingsRequest request) {
        DistrictPriceSettingsResponse response = districtPriceSettingsService.createDistrictPriceSettings(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/district/{districtId}")
    public ResponseEntity<DistrictPriceSettingsResponse> updateDistrictPriceSettings(
            @PathVariable Long districtId,
            @Valid @RequestBody DistrictPriceSettingsRequest request) {
        DistrictPriceSettingsResponse response = districtPriceSettingsService.updateDistrictPriceSettings(districtId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/fallback")
    public ResponseEntity<FallbackPriceSettings> createFallbackPriceSettings(
            @Valid @RequestBody FallbackPriceSettingsRequest request) {
        FallbackPriceSettings response = districtPriceSettingsService.createFallbackPriceSettings(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/fallback")
    public ResponseEntity<FallbackPriceSettings> updateFallbackPriceSettings(
            @Valid @RequestBody FallbackPriceSettingsRequest request) {
        FallbackPriceSettings response = districtPriceSettingsService.updateFallbackPriceSettings(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/fallback")
    public ResponseEntity<List<FallbackPriceSettings>> getAllFallbackPriceSettings() {
        List<FallbackPriceSettings> response = districtPriceSettingsService.getAllFallbackPriceSettings();
        return ResponseEntity.ok(response);
    }
}
