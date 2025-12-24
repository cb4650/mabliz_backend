package com.dztech.rayder.repository;

import com.dztech.rayder.model.SavedAddress;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavedAddressRepository extends JpaRepository<SavedAddress, Long> {
    List<SavedAddress> findByUserIdOrderByIdAsc(Long userId);

    Optional<SavedAddress> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndLabel(Long userId, String label);

    boolean existsByUserIdAndLabelAndIdNot(Long userId, String label, Long id);
}
