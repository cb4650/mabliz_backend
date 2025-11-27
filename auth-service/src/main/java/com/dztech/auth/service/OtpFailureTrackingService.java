package com.dztech.auth.service;

import com.dztech.auth.model.OtpFailureTracking;
import com.dztech.auth.repository.OtpFailureTrackingRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OtpFailureTrackingService {

    private static final int MAX_FAILURES = 4;
    private static final Duration BLOCK_DURATION = Duration.ofMinutes(5);

    private final OtpFailureTrackingRepository repository;

    public OtpFailureTrackingService(OtpFailureTrackingRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void recordOtpFailure(String phone, OtpFailureTracking.RoleType roleType) {
        Instant now = Instant.now();

        Optional<OtpFailureTracking> trackingOpt = repository.findByPhoneAndRoleType(phone, roleType);

        if (trackingOpt.isPresent()) {
            OtpFailureTracking tracking = trackingOpt.get();
            tracking.setFailureCount(tracking.getFailureCount() + 1);
            tracking.setLastFailureAt(now);

            // If reached max failures, block for the duration
            if (tracking.getFailureCount() >= MAX_FAILURES) {
                tracking.setBlockedUntil(now.plus(BLOCK_DURATION));
            }

            repository.save(tracking);
        } else {
            // First failure for this phone/role combination
            OtpFailureTracking tracking = OtpFailureTracking.builder()
                    .phone(phone)
                    .roleType(roleType)
                    .failureCount(1)
                    .lastFailureAt(now)
                    .build();
            repository.save(tracking);
        }
    }

    @Transactional
    public void resetOtpFailures(String phone, OtpFailureTracking.RoleType roleType) {
        repository.resetFailureCount(phone, roleType);
    }

    @Transactional(readOnly = true)
    public boolean isOtpBlocked(String phone, OtpFailureTracking.RoleType roleType) {
        Optional<OtpFailureTracking> trackingOpt = repository.findByPhoneAndRoleType(phone, roleType);
        return trackingOpt.isPresent() && trackingOpt.get().isBlocked();
    }

    @Transactional(readOnly = true)
    public Optional<Duration> getRemainingBlockTime(String phone, OtpFailureTracking.RoleType roleType) {
        Optional<OtpFailureTracking> trackingOpt = repository.findByPhoneAndRoleType(phone, roleType);
        if (trackingOpt.isPresent()) {
            OtpFailureTracking tracking = trackingOpt.get();
            if (tracking.isBlocked()) {
                Instant now = Instant.now();
                Duration remaining = Duration.between(now, tracking.getBlockedUntil());
                return Optional.of(remaining);
            }
        }
        return Optional.empty();
    }

    /**
     * Scheduled task to clean up expired blocks every minute
     * This helps prevent database from growing too large with expired records
     */
    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
    @Transactional
    public void cleanupExpiredBlocks() {
        repository.unblockExpiredEntries();
    }

    /**
     * Get failure information for admin monitoring (optional)
     */
    @Transactional(readOnly = true)
    public Optional<OtpFailureTracking> getFailureInfo(String phone, OtpFailureTracking.RoleType roleType) {
        return repository.findByPhoneAndRoleType(phone, roleType);
    }
}
