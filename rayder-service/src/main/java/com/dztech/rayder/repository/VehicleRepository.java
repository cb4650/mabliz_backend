package com.dztech.rayder.repository;

import com.dztech.rayder.model.Vehicle;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    @EntityGraph(attributePaths = {"brand", "model"})
    List<Vehicle> findByUserIdOrderByIdAsc(Long userId);

    @EntityGraph(attributePaths = {"brand", "model"})
    Optional<Vehicle> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);
}
