package com.dztech.auth.repository;

import com.dztech.auth.model.DistrictPriceSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DistrictPriceSettingsRepository extends JpaRepository<DistrictPriceSettings, Long> {

    Optional<DistrictPriceSettings> findByDistrictIdAndModelType(Long districtId, DistrictPriceSettings.ModelType modelType);

    List<DistrictPriceSettings> findByDistrictId(Long districtId);

    List<DistrictPriceSettings> findByModelType(DistrictPriceSettings.ModelType modelType);

    @Query("SELECT dps FROM DistrictPriceSettings dps JOIN FETCH dps.district WHERE dps.district.isActive = true")
    List<DistrictPriceSettings> findAllWithActiveDistricts();

    @Query("SELECT dps FROM DistrictPriceSettings dps JOIN FETCH dps.district WHERE dps.district.id = :districtId")
    List<DistrictPriceSettings> findByDistrictIdWithDistrict(@Param("districtId") Long districtId);

    boolean existsByDistrictIdAndModelType(Long districtId, DistrictPriceSettings.ModelType modelType);
}
