package com.dztech.auth.repository;

import com.dztech.auth.model.About;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AboutRepository extends JpaRepository<About, Long> {

    Optional<About> findByAppId(String appId);

    boolean existsByAppId(String appId);
}
