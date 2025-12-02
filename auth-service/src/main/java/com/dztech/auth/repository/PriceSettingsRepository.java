package com.dztech.auth.repository;

import com.dztech.auth.model.PriceSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PriceSettingsRepository extends JpaRepository<PriceSettings, Long> {

    PriceSettings findByClassName(String className);
}
