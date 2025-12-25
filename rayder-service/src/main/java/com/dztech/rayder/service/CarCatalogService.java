package com.dztech.rayder.service;

import com.dztech.rayder.dto.CarBrandResponse;
import com.dztech.rayder.dto.CarModelResponse;
import com.dztech.rayder.exception.ResourceNotFoundException;
import com.dztech.rayder.model.CarBrand;
import com.dztech.rayder.model.CarModel;
import com.dztech.rayder.repository.CarBrandRepository;
import com.dztech.rayder.repository.CarModelRepository;
import java.util.List;
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
    public List<CarBrandResponse> getAllBrands() {
        return carBrandRepository.findAllByOrderByIdAsc().stream()
                .map(this::toBrandResponse)
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
                brand.getId(),
                brand.getName(),
                brand.getCountry(),
                brand.getCategory(),
                brand.getBrandImageUrl());
    }

    private CarModelResponse toModelResponse(CarModel model) {
        return new CarModelResponse(model.getId(), model.getBrand().getId(), model.getName());
    }
}
