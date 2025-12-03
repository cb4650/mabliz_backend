package com.dztech.auth.controller;

import com.dztech.auth.dto.PriceSettingsResponse;
import com.dztech.auth.dto.UpdatePriceSettingsRequest;
import com.dztech.auth.dto.UpdatePriceSettingsResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/price-settings")
public class PriceSettingsController {

    private final com.dztech.auth.service.PriceSettingsService priceSettingsService;

    public PriceSettingsController(com.dztech.auth.service.PriceSettingsService priceSettingsService) {
        this.priceSettingsService = priceSettingsService;
    }

    @GetMapping
    public ResponseEntity<PriceSettingsResponse> getPriceSettings() {
        PriceSettingsResponse response = priceSettingsService.getAllPriceSettings();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{classId}")
    public ResponseEntity<UpdatePriceSettingsResponse> updatePriceSettings(
            @PathVariable Long classId,
            @Valid @RequestBody UpdatePriceSettingsRequest request) {
        UpdatePriceSettingsResponse response = priceSettingsService.updatePriceSettings(classId, request);
        return ResponseEntity.ok(response);
    }
}
