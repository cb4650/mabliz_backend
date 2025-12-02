package com.dztech.rayder.service;

import com.dztech.rayder.dto.ForceUpdateResponse;
import com.dztech.rayder.dto.UpdateForceUpdateRequest;
import com.dztech.rayder.exception.ResourceNotFoundException;
import com.dztech.rayder.model.ForceUpdate;
import com.dztech.rayder.repository.ForceUpdateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ForceUpdateService {

    private final ForceUpdateRepository forceUpdateRepository;

    public ForceUpdateService(ForceUpdateRepository forceUpdateRepository) {
        this.forceUpdateRepository = forceUpdateRepository;
    }

    @Transactional(readOnly = true)
    public ForceUpdateResponse getVersionByAppIdAndPlatform(String appId, ForceUpdate.Platform platform) {
        ForceUpdate forceUpdate = forceUpdateRepository.findByAppIdAndPlatform(appId, platform)
                .orElseThrow(() -> new ResourceNotFoundException("Version not found for appId: " + appId + ", platform: " + platform));
        return ForceUpdateResponse.fromEntity(forceUpdate);
    }

    @Transactional
    public ForceUpdateResponse updateVersion(String appId, ForceUpdate.Platform platform, UpdateForceUpdateRequest request) {
        ForceUpdate forceUpdate = forceUpdateRepository.findByAppIdAndPlatform(appId, platform)
                .orElseThrow(() -> new ResourceNotFoundException("Version not found for appId: " + appId + ", platform: " + platform));

        forceUpdate.setVersion(request.version());
        ForceUpdate saved = forceUpdateRepository.save(forceUpdate);
        return ForceUpdateResponse.fromEntity(saved);
    }
}
