package com.dztech.auth.repository;

import com.dztech.auth.model.DriverProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DriverProfileRepository extends JpaRepository<DriverProfile, Long> {
    Optional<DriverProfile> findByEmail(String email);
}
