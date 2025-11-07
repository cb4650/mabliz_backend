package com.dztech.auth.repository;

import com.dztech.auth.model.DriverVehicle;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DriverVehicleRepository extends JpaRepository<DriverVehicle, Long> {
    boolean existsByVehicleNumberIgnoreCase(String vehicleNumber);

    List<DriverVehicle> findByUserIdOrderByCreatedAtDesc(Long userId);
}
