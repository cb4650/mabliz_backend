package com.dztech.auth.service;

import com.dztech.auth.dto.DistrictPriceSettingsRequest;
import com.dztech.auth.dto.DistrictPriceSettingsResponse;
import com.dztech.auth.dto.FallbackPriceSettingsRequest;
import com.dztech.auth.model.District;
import com.dztech.auth.model.DistrictPriceSettings;
import com.dztech.auth.model.FallbackPriceSettings;
import com.dztech.auth.repository.DistrictPriceSettingsRepository;
import com.dztech.auth.repository.DistrictRepository;
import com.dztech.auth.repository.FallbackPriceSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DistrictPriceSettingsService {

    private final DistrictPriceSettingsRepository districtPriceSettingsRepository;
    private final FallbackPriceSettingsRepository fallbackPriceSettingsRepository;
    private final DistrictRepository districtRepository;

    @Transactional(readOnly = true)
    public List<DistrictPriceSettingsResponse> getAllDistrictPriceSettings() {
        List<DistrictPriceSettings> settings = districtPriceSettingsRepository.findAllWithActiveDistricts();
        return settings.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DistrictPriceSettingsResponse> getDistrictPriceSettings(Long districtId) {
        List<DistrictPriceSettings> settings = districtPriceSettingsRepository.findByDistrictIdWithDistrict(districtId);
        return settings.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DistrictPriceSettingsResponse createDistrictPriceSettings(DistrictPriceSettingsRequest request) {
        District district = districtRepository.findById(request.getDistrictId())
                .orElseThrow(() -> new IllegalArgumentException("District not found: " + request.getDistrictId()));

        if (!district.getIsActive()) {
            throw new IllegalArgumentException("District is not active: " + district.getName());
        }

        DistrictPriceSettings.ModelType modelType = DistrictPriceSettings.ModelType.valueOf(request.getModelType().name());

        if (districtPriceSettingsRepository.existsByDistrictIdAndModelType(request.getDistrictId(), modelType)) {
            throw new IllegalArgumentException("Price settings already exist for district " + district.getName() + " and model type " + request.getModelType());
        }

        DistrictPriceSettings settings = DistrictPriceSettings.builder()
                .district(district)
                .modelType(modelType)
                .minimumHours(request.getMinimumHours())
                .baseFare(request.getBaseFare())
                .extraFarePerHour(request.getExtraFarePerHour())
                .nightCharges(request.getNightCharges())
                .festiveCharges(request.getFestiveCharges())
                .rainCharge(request.getRainCharge())
                .dropChargesPer5km(request.getDropChargesPer5km())
                .dropChargesPer100km(request.getDropChargesPer100km())
                .dropLimitKms(request.getDropLimitKms())
                .driverCancellationPenalty(request.getDriverCancellationPenalty())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        DistrictPriceSettings saved = districtPriceSettingsRepository.save(settings);
        return mapToResponse(saved);
    }

    @Transactional
    public DistrictPriceSettingsResponse updateDistrictPriceSettings(Long districtId, DistrictPriceSettingsRequest request) {
        DistrictPriceSettings.ModelType modelType = DistrictPriceSettings.ModelType.valueOf(request.getModelType().name());

        DistrictPriceSettings existing = districtPriceSettingsRepository
                .findByDistrictIdAndModelType(districtId, modelType)
                .orElseThrow(() -> new IllegalArgumentException("Price settings not found for district " + districtId + " and model type " + request.getModelType()));

        DistrictPriceSettings updated = DistrictPriceSettings.builder()
                .id(existing.getId())
                .district(existing.getDistrict())
                .modelType(modelType)
                .minimumHours(request.getMinimumHours())
                .baseFare(request.getBaseFare())
                .extraFarePerHour(request.getExtraFarePerHour())
                .nightCharges(request.getNightCharges())
                .festiveCharges(request.getFestiveCharges())
                .rainCharge(request.getRainCharge())
                .dropChargesPer5km(request.getDropChargesPer5km())
                .dropChargesPer100km(request.getDropChargesPer100km())
                .dropLimitKms(request.getDropLimitKms())
                .driverCancellationPenalty(request.getDriverCancellationPenalty())
                .createdAt(existing.getCreatedAt())
                .updatedAt(Instant.now())
                .build();

        DistrictPriceSettings saved = districtPriceSettingsRepository.save(updated);
        return mapToResponse(saved);
    }

    @Transactional
    public FallbackPriceSettings createFallbackPriceSettings(FallbackPriceSettingsRequest request) {
        FallbackPriceSettings.ModelType modelType = FallbackPriceSettings.ModelType.valueOf(request.getModelType().name());

        if (fallbackPriceSettingsRepository.existsByModelType(modelType)) {
            throw new IllegalArgumentException("Fallback price settings already exist for model type " + request.getModelType());
        }

        FallbackPriceSettings settings = FallbackPriceSettings.builder()
                .modelType(modelType)
                .minimumHours(request.getMinimumHours())
                .baseFare(request.getBaseFare())
                .extraFarePerHour(request.getExtraFarePerHour())
                .nightCharges(request.getNightCharges())
                .festiveCharges(request.getFestiveCharges())
                .rainCharge(request.getRainCharge())
                .dropChargesPer5km(request.getDropChargesPer5km())
                .dropChargesPer100km(request.getDropChargesPer100km())
                .dropLimitKms(request.getDropLimitKms())
                .driverCancellationPenalty(request.getDriverCancellationPenalty())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        return fallbackPriceSettingsRepository.save(settings);
    }

    @Transactional
    public FallbackPriceSettings updateFallbackPriceSettings(FallbackPriceSettingsRequest request) {
        FallbackPriceSettings.ModelType modelType = FallbackPriceSettings.ModelType.valueOf(request.getModelType().name());

        FallbackPriceSettings existing = fallbackPriceSettingsRepository
                .findByModelType(modelType)
                .orElseThrow(() -> new IllegalArgumentException("Fallback price settings not found for model type " + request.getModelType()));

        FallbackPriceSettings updated = FallbackPriceSettings.builder()
                .id(existing.getId())
                .modelType(modelType)
                .minimumHours(request.getMinimumHours())
                .baseFare(request.getBaseFare())
                .extraFarePerHour(request.getExtraFarePerHour())
                .nightCharges(request.getNightCharges())
                .festiveCharges(request.getFestiveCharges())
                .rainCharge(request.getRainCharge())
                .dropChargesPer5km(request.getDropChargesPer5km())
                .dropChargesPer100km(request.getDropChargesPer100km())
                .dropLimitKms(request.getDropLimitKms())
                .driverCancellationPenalty(request.getDriverCancellationPenalty())
                .createdAt(existing.getCreatedAt())
                .updatedAt(Instant.now())
                .build();

        return fallbackPriceSettingsRepository.save(updated);
    }

    @Transactional(readOnly = true)
    public List<FallbackPriceSettings> getAllFallbackPriceSettings() {
        return fallbackPriceSettingsRepository.findAll();
    }

    private DistrictPriceSettingsResponse mapToResponse(DistrictPriceSettings settings) {
        return DistrictPriceSettingsResponse.builder()
                .id(settings.getId())
                .districtId(settings.getDistrict().getId())
                .districtName(settings.getDistrict().getName())
                .modelType(DistrictPriceSettingsResponse.ModelType.valueOf(settings.getModelType().name()))
                .minimumHours(settings.getMinimumHours())
                .baseFare(settings.getBaseFare())
                .extraFarePerHour(settings.getExtraFarePerHour())
                .nightCharges(settings.getNightCharges())
                .festiveCharges(settings.getFestiveCharges())
                .rainCharge(settings.getRainCharge())
                .dropChargesPer5km(settings.getDropChargesPer5km())
                .dropChargesPer100km(settings.getDropChargesPer100km())
                .dropLimitKms(settings.getDropLimitKms())
                .driverCancellationPenalty(settings.getDriverCancellationPenalty())
                .createdAt(settings.getCreatedAt())
                .updatedAt(settings.getUpdatedAt())
                .build();
    }
}
