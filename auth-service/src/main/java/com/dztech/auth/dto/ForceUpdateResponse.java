package com.dztech.auth.dto;

import com.dztech.auth.model.ForceUpdate;

public record ForceUpdateResponse(
        String appId,
        String platform,
        String version) {

    public static ForceUpdateResponse fromEntity(ForceUpdate forceUpdate) {
        return new ForceUpdateResponse(
                forceUpdate.getAppId(),
                forceUpdate.getPlatform().name(),
                forceUpdate.getVersion()
        );
    }
}
