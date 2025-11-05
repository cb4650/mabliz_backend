package com.dztech.auth.config;

import com.dztech.auth.model.AppId;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "auth.login")
public class LoginProfileProperties {

    private Map<String, ProfileType> profileTypes = new HashMap<>();
    private ProfileType defaultProfileType = ProfileType.USER;

    public ProfileType resolve(AppId appId) {
        ProfileType explicit = profileTypes.get(appId.value());
        return explicit != null ? explicit : defaultProfileType;
    }

    public Map<String, ProfileType> getProfileTypes() {
        return profileTypes;
    }

    public void setProfileTypes(Map<String, ProfileType> profileTypes) {
        this.profileTypes = profileTypes != null ? profileTypes : new HashMap<>();
    }

    public ProfileType getDefaultProfileType() {
        return defaultProfileType;
    }

    public void setDefaultProfileType(ProfileType defaultProfileType) {
        if (defaultProfileType != null) {
            this.defaultProfileType = defaultProfileType;
        }
    }

    public enum ProfileType {
        USER,
        DRIVER
    }
}
