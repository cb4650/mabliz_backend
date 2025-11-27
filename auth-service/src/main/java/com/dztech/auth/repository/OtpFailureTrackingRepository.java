package com.dztech.auth.repository;

import com.dztech.auth.model.OtpFailureTracking;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OtpFailureTrackingRepository extends JpaRepository<OtpFailureTracking, Long> {

    Optional<OtpFailureTracking> findByPhoneAndRoleType(String phone, OtpFailureTracking.RoleType roleType);

    @Modifying
    @Query("UPDATE OtpFailureTracking ft SET ft.failureCount = 0, ft.blockedUntil = NULL WHERE ft.phone = :phone AND ft.roleType = :roleType")
    void resetFailureCount(@Param("phone") String phone, @Param("roleType") OtpFailureTracking.RoleType roleType);

    @Modifying
    @Query("UPDATE OtpFailureTracking ft SET ft.blockedUntil = NULL WHERE ft.blockedUntil IS NOT NULL AND ft.blockedUntil < CURRENT_TIMESTAMP")
    void unblockExpiredEntries();
}
