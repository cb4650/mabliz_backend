package com.dztech.auth.repository;

import com.dztech.auth.model.FallbackPriceSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FallbackPriceSettingsRepository extends JpaRepository<FallbackPriceSettings, Long> {

    Optional<FallbackPriceSettings> findByModelType(FallbackPriceSettings.ModelType modelType);

    boolean existsByModelType(FallbackPriceSettings.ModelType modelType);
}
