package com.dztech.auth.repository;

import com.dztech.auth.model.DriverEmailVerificationToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DriverEmailVerificationTokenRepository
        extends JpaRepository<DriverEmailVerificationToken, Long> {
    Optional<DriverEmailVerificationToken> findByEmail(String email);

    Optional<DriverEmailVerificationToken> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
