package com.dztech.auth.repository;

import com.dztech.auth.model.EmailVerificationToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    Optional<EmailVerificationToken> findFirstByUserIdAndEmailOrderByCreatedAtDesc(Long userId, String email);

    void deleteByUserIdAndEmail(Long userId, String email);
}
