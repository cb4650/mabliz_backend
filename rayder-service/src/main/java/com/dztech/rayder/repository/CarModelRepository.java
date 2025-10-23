package com.dztech.rayder.repository;

import com.dztech.rayder.model.CarModel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarModelRepository extends JpaRepository<CarModel, Long> {
    List<CarModel> findByBrand_IdOrderByIdAsc(Long brandId);
}
