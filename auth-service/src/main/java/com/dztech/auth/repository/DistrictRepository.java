package com.dztech.auth.repository;

import com.dztech.auth.model.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DistrictRepository extends JpaRepository<District, Long> {

    Optional<District> findByNameIgnoreCase(String name);

    List<District> findByIsActiveTrue();

    boolean existsByNameIgnoreCase(String name);
}
