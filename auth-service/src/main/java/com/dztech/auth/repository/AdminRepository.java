package com.dztech.auth.repository;

import com.dztech.auth.model.Admin;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByPhone(String phone);

    boolean existsByPhone(String phone);
}
