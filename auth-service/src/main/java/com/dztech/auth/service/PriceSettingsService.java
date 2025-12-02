package com.dztech.auth.service;

import com.dztech.auth.dto.ClassPriceSettingsView;
import com.dztech.auth.dto.CommissionSettingsView;
import com.dztech.auth.dto.PriceSettingsResponse;
import com.dztech.auth.dto.PriceSettingsView;
import com.dztech.auth.dto.UpdatePriceSettingsRequest;
import com.dztech.auth.exception.ResourceNotFoundException;
import com.dztech.auth.model.PriceSettings;
import com.dztech.auth.repository.PriceSettingsRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PriceSettingsService {

    private final PriceSettingsRepository priceSettingsRepository;

    public PriceSettingsService(PriceSettingsRepository priceSettingsRepository) {
        this.priceSettingsRepository = priceSettingsRepository;
    }

    @Transactional(readOnly = true)
    public PriceSettingsResponse getAllPriceSettings() {
        List<PriceSettings> priceSettings = priceSettingsRepository.findAll();

        List<ClassPriceSettingsView> classes = priceSettings.stream()
                .map(this::mapToClassPriceSettingsView)
                .collect(Collectors.toList());

        return PriceSettingsResponse.builder()
                .status("success")
                .data(new PriceSettingsResponse.PriceSettingsData(classes))
                .build();
    }

    @Transactional
    public void updatePriceSettings(Long classId, UpdatePriceSettingsRequest request) {
        PriceSettings priceSettings = priceSettingsRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Price settings not found for class ID: " + classId));

        // Update price settings
        UpdatePriceSettingsRequest.PriceSettingsUpdate priceUpdate = request.getPriceSettings();
        priceSettings.setBaseFare(priceUpdate.getBaseFare());
        priceSettings.setPerHour(priceUpdate.getPerHour());
        priceSettings.setLateNightCharges(priceUpdate.getLateNightCharges());
        priceSettings.setExtraHourCharges(priceUpdate.getExtraHourCharges());
        priceSettings.setFoodCharges(priceUpdate.getFoodCharges());
        priceSettings.setFestivalCharges(priceUpdate.getFestivalCharges());

        // Update commission settings
        UpdatePriceSettingsRequest.CommissionSettingsUpdate commissionUpdate = request.getCommissionSettings();
        priceSettings.setPlatformCommission(commissionUpdate.getPlatformCommission());
        priceSettings.setFestivalCommission(commissionUpdate.getFestivalCommission());

        priceSettingsRepository.save(priceSettings);
    }

    private ClassPriceSettingsView mapToClassPriceSettingsView(PriceSettings entity) {
        PriceSettingsView priceSettingsView = PriceSettingsView.builder()
                .baseFare(entity.getBaseFare())
                .perHour(entity.getPerHour())
                .lateNightCharges(entity.getLateNightCharges())
                .extraHourCharges(entity.getExtraHourCharges())
                .foodCharges(entity.getFoodCharges())
                .festivalCharges(entity.getFestivalCharges())
                .build();

        CommissionSettingsView commissionSettingsView = CommissionSettingsView.builder()
                .platformCommission(entity.getPlatformCommission())
                .festivalCommission(entity.getFestivalCommission())
                .build();

        return ClassPriceSettingsView.builder()
                .id(entity.getId().toString())
                .name(entity.getClassName())
                .priceSettings(priceSettingsView)
                .commissionSettings(commissionSettingsView)
                .build();
    }
}
