package com.dztech.rayder.repository;

import com.dztech.rayder.model.DriverTripResponse;
import com.dztech.rayder.model.DriverTripResponseStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DriverTripResponseRepository extends JpaRepository<DriverTripResponse, Long> {
    Optional<DriverTripResponse> findByBookingIdAndDriverId(Long bookingId, Long driverId);

    boolean existsByBookingIdAndStatus(Long bookingId, DriverTripResponseStatus status);
}
