package com.dztech.auth.repository;

import com.dztech.auth.model.DriverFieldVerification;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DriverFieldVerificationRepository extends JpaRepository<DriverFieldVerification, Long> {

    List<DriverFieldVerification> findByDriverId(Long driverId);

    List<DriverFieldVerification> findByDriverIdIn(Collection<Long> driverIds);

    Optional<DriverFieldVerification> findByDriverIdAndFieldNameIgnoreCase(Long driverId, String fieldName);
}
