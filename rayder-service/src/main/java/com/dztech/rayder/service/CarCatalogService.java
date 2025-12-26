package com.dztech.rayder.service;

import com.dztech.rayder.dto.CarBrandCategoryResponse;
import com.dztech.rayder.dto.CarBrandResponse;
import com.dztech.rayder.dto.CarModelResponse;
import com.dztech.rayder.exception.ResourceNotFoundException;
import com.dztech.rayder.model.CarBrand;
import com.dztech.rayder.model.CarModel;
import com.dztech.rayder.repository.CarBrandRepository;
import com.dztech.rayder.repository.CarModelRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CarCatalogService {

    private final CarBrandRepository carBrandRepository;
    private final CarModelRepository carModelRepository;

    public CarCatalogService(
            CarBrandRepository carBrandRepository, CarModelRepository carModelRepository) {
        this.carBrandRepository = carBrandRepository;
        this.carModelRepository = carModelRepository;
    }

    @Transactional(readOnly = true)
    public List<CarBrandCategoryResponse> getAllBrands() {
        List<CarBrand> brands = carBrandRepository.findAllByOrderByIdAsc();
        Map<String, List<CarBrandResponse>> groupedBrands = new LinkedHashMap<>();

        for (CarBrand brand : brands) {
            groupedBrands
                    .computeIfAbsent(brand.getCategory(), ignored -> new ArrayList<>())
                    .add(toBrandResponse(brand));
        }

        return groupedBrands.entrySet().stream()
                .map(entry -> new CarBrandCategoryResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CarModelResponse> getModelsByBrand(Long brandId) {
        CarBrand brand = carBrandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("Car brand not found"));
        return carModelRepository.findByBrand_IdOrderByIdAsc(brand.getId()).stream()
                .map(this::toModelResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CarModel getModelById(Long modelId) {
        return carModelRepository.findById(modelId)
                .orElseThrow(() -> new ResourceNotFoundException("Car model not found"));
    }

    @Transactional(readOnly = true)
    public CarBrand getBrandById(Long brandId) {
        return carBrandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("Car brand not found"));
    }

    private CarBrandResponse toBrandResponse(CarBrand brand) {
        return new CarBrandResponse(
                brand.getId(), brand.getName(), brand.getCountry(), brand.getBrandImageUrl());
    }

    private CarModelResponse toModelResponse(CarModel model) {
        return new CarModelResponse(model.getId(), model.getBrand().getId(), model.getName());
    }
}
