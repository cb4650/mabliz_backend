package com.dztech.rayder.controller;

import com.dztech.rayder.dto.CarBrandListResponse;
import com.dztech.rayder.dto.CarModelListResponse;
import com.dztech.rayder.dto.CreateVehicleRequest;
import com.dztech.rayder.dto.UpdateVehicleRequest;
import com.dztech.rayder.dto.VehicleDeleteResponse;
import com.dztech.rayder.dto.VehicleListResponse;
import com.dztech.rayder.dto.VehicleOperationResponse;
import com.dztech.rayder.dto.VehicleResponse;
import com.dztech.rayder.security.AuthenticatedUserProvider;
import com.dztech.rayder.service.CarCatalogService;
import com.dztech.rayder.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final CarCatalogService carCatalogService;

    public VehicleController(
            VehicleService vehicleService,
            AuthenticatedUserProvider authenticatedUserProvider,
            CarCatalogService carCatalogService) {
        this.vehicleService = vehicleService;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.carCatalogService = carCatalogService;
    }

    @GetMapping("/brands")
    public ResponseEntity<CarBrandListResponse> getBrands() {
        CarBrandListResponse response = new CarBrandListResponse(true, carCatalogService.getAllBrands());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/brands/{brandId}/models")
    public ResponseEntity<CarModelListResponse> getModels(@PathVariable("brandId") Long brandId) {
        CarModelListResponse response =
                new CarModelListResponse(true, carCatalogService.getModelsByBrand(brandId));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/add")
    public ResponseEntity<VehicleOperationResponse> addVehicle(@RequestBody @Valid CreateVehicleRequest request) {
        Long userId = authenticatedUserProvider.getCurrentUserId();
        VehicleResponse response = vehicleService.addVehicle(userId, request);
        VehicleOperationResponse body =
                new VehicleOperationResponse(true, "Vehicle added successfully", response);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping
    public ResponseEntity<VehicleListResponse> listVehicles() {
        Long userId = authenticatedUserProvider.getCurrentUserId();
        VehicleListResponse response =
                new VehicleListResponse(true, vehicleService.getVehicles(userId));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehicleOperationResponse> updateVehicle(
            @PathVariable("id") Long id, @RequestBody @Valid UpdateVehicleRequest request) {
        Long userId = authenticatedUserProvider.getCurrentUserId();
        VehicleResponse response = vehicleService.updateVehicle(userId, id, request);
        VehicleOperationResponse body =
                new VehicleOperationResponse(true, "Vehicle updated successfully", response);
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<VehicleDeleteResponse> deleteVehicle(@PathVariable("id") Long id) {
        Long userId = authenticatedUserProvider.getCurrentUserId();
        vehicleService.deleteVehicle(userId, id);
        return ResponseEntity.ok(new VehicleDeleteResponse(true, "Vehicle deleted successfully"));
    }
}
