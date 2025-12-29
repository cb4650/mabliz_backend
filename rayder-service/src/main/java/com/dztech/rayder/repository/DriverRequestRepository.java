package com.dztech.rayder.repository;

import com.dztech.rayder.model.DriverRequest;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DriverRequestRepository extends JpaRepository<DriverRequest, Long> {
    Optional<DriverRequest> findByIdAndUserId(Long id, Long userId);

    @Modifying
    @Query("""
            update DriverRequest dr
            set dr.acceptedDriverId = :driverId, dr.acceptedAt = :acceptedAt
            where dr.id = :bookingId and (dr.acceptedDriverId is null or dr.acceptedDriverId = :driverId)
            """)
    int assignDriverIfAvailable(
            @Param("bookingId") Long bookingId,
            @Param("driverId") Long driverId,
            @Param("acceptedAt") java.time.Instant acceptedAt);
}
