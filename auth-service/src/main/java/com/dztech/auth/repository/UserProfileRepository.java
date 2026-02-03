package com.dztech.auth.repository;

import com.dztech.auth.model.UserProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByUserId(Long userId);

    Optional<UserProfile> findByPhone(String phone);

    boolean existsByEmailAndUserIdNot(String email, Long userId);

    boolean existsByPhoneAndUserIdNot(String phone, Long userId);
}
