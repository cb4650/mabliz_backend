package com.dztech.rayder.dto;

import com.dztech.rayder.model.ForceUpdate;

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
