package com.dztech.rayder.repository;

import com.dztech.rayder.model.DriverRequest;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DriverRequestRepository extends JpaRepository<DriverRequest, Long> {
    Optional<DriverRequest> findByIdAndUserId(Long id, Long userId);
}
