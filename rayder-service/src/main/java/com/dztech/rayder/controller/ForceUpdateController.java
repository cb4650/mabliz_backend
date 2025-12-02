package com.dztech.rayder.controller;

import com.dztech.rayder.dto.ForceUpdateResponse;
import com.dztech.rayder.dto.UpdateForceUpdateRequest;
import com.dztech.rayder.model.ForceUpdate;
import com.dztech.rayder.service.ForceUpdateService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/force-update")
public class ForceUpdateController {

    private final ForceUpdateService forceUpdateService;

    public ForceUpdateController(ForceUpdateService forceUpdateService) {
        this.forceUpdateService = forceUpdateService;
    }

    @GetMapping("/{platform}")
    public ResponseEntity<ForceUpdateResponse> getVersion(
            @RequestHeader("appId") String appId,
            @PathVariable("platform") String platformStr) {
        try {
            ForceUpdate.Platform platform = ForceUpdate.Platform.valueOf(platformStr.toUpperCase());
            ForceUpdateResponse response = forceUpdateService.getVersionByAppIdAndPlatform(appId, platform);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid platform: " + platformStr + ". Supported platforms: ANDROID, IOS");
        }
    }

    @PutMapping("/{platform}")
    public ResponseEntity<ForceUpdateResponse> updateVersion(
            @RequestHeader("appId") String appId,
            @PathVariable("platform") String platformStr,
            @RequestBody @Valid UpdateForceUpdateRequest request) {
        try {
            ForceUpdate.Platform platform = ForceUpdate.Platform.valueOf(platformStr.toUpperCase());
            ForceUpdateResponse response = forceUpdateService.updateVersion(appId, platform, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid platform: " + platformStr + ". Supported platforms: ANDROID, IOS");
        }
    }
}
