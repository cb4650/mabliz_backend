package com.dztech.rayder.repository;

import com.dztech.rayder.model.CarBrand;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarBrandRepository extends JpaRepository<CarBrand, Long> {
    List<CarBrand> findAllByOrderByIdAsc();
}
