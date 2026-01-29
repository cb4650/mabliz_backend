package com.dztech.auth.service;

import com.dztech.auth.dto.AboutRequest;
import com.dztech.auth.dto.AboutResponse;
import com.dztech.auth.model.About;
import com.dztech.auth.repository.AboutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AboutService {

    private final AboutRepository aboutRepository;

    @Transactional(readOnly = true)
    public AboutResponse getAboutByAppId(String appId) {
        validateAppId(appId);
        About about = aboutRepository.findByAppId(appId)
                .orElseThrow(() -> new IllegalArgumentException("About content not found for app ID: " + appId));

        return AboutResponse.builder()
                .success(true)
                .data(AboutResponse.AboutData.builder()
                        .title(about.getTitle())
                        .description(about.getDescription())
                        .build())
                .build();
    }

    @Transactional
    public AboutResponse createAbout(AboutRequest request) {
        validateAppId(request.getAppId());

        if (aboutRepository.existsByAppId(request.getAppId())) {
            throw new IllegalArgumentException("About content already exists for app ID: " + request.getAppId());
        }

        About about = About.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .appId(request.getAppId())
                .build();

        About saved = aboutRepository.save(about);
        return AboutResponse.builder()
                .success(true)
                .data(AboutResponse.AboutData.builder()
                        .title(saved.getTitle())
                        .description(saved.getDescription())
                        .build())
                .build();
    }

    @Transactional
    public AboutResponse updateAbout(String appId, AboutRequest request) {
        validateAppId(appId);
        validateAppId(request.getAppId());

        if (!appId.equals(request.getAppId())) {
            throw new IllegalArgumentException("App ID in path must match app ID in request body");
        }

        About existing = aboutRepository.findByAppId(appId)
                .orElseThrow(() -> new IllegalArgumentException("About content not found for app ID: " + appId));

        About updated = About.builder()
                .id(existing.getId())
                .title(request.getTitle())
                .description(request.getDescription())
                .appId(request.getAppId())
                .createdAt(existing.getCreatedAt())
                .build();

        About saved = aboutRepository.save(updated);
        return AboutResponse.builder()
                .success(true)
                .data(AboutResponse.AboutData.builder()
                        .title(saved.getTitle())
                        .description(saved.getDescription())
                        .build())
                .build();
    }

    @Transactional
    public void deleteAbout(String appId) {
        validateAppId(appId);

        About about = aboutRepository.findByAppId(appId)
                .orElseThrow(() -> new IllegalArgumentException("About content not found for app ID: " + appId));

        aboutRepository.delete(about);
    }

    private void validateAppId(String appId) {
        if (!"rydd".equals(appId) && !"rydc".equals(appId)) {
            throw new IllegalArgumentException("Invalid app ID. Must be 'rydd' or 'rydc'");
        }
    }
}
