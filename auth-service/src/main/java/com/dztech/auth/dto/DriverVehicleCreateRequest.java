package com.dztech.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public class DriverVehicleCreateRequest {

    @NotBlank(message = "vehicleNumber is required")
    @Size(max = 25, message = "vehicleNumber must be at most 25 characters")
    private String vehicleNumber;

    @NotBlank(message = "vehicleType is required")
    private String vehicleType;

    @NotBlank(message = "manufacturedYear is required")
    @Size(max = 7, message = "manufacturedYear must be at most 7 characters")
    private String manufacturedYear;

    @NotBlank(message = "insuranceExpiryDate is required")
    private String insuranceExpiryDate;

    @NotBlank(message = "brand is required")
    @Size(max = 100, message = "brand must be at most 100 characters")
    private String brand;

    @NotBlank(message = "model is required")
    @Size(max = 100, message = "model must be at most 100 characters")
    private String model;

    @NotNull(message = "rcImage is required")
    private MultipartFile rcImage;

    @NotNull(message = "insuranceImage is required")
    private MultipartFile insuranceImage;

    @NotNull(message = "pollutionCertificateImage is required")
    private MultipartFile pollutionCertificateImage;

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getManufacturedYear() {
        return manufacturedYear;
    }

    public void setManufacturedYear(String manufacturedYear) {
        this.manufacturedYear = manufacturedYear;
    }

    public String getInsuranceExpiryDate() {
        return insuranceExpiryDate;
    }

    public void setInsuranceExpiryDate(String insuranceExpiryDate) {
        this.insuranceExpiryDate = insuranceExpiryDate;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public MultipartFile getRcImage() {
        return rcImage;
    }

    public void setRcImage(MultipartFile rcImage) {
        this.rcImage = rcImage;
    }

    public MultipartFile getInsuranceImage() {
        return insuranceImage;
    }

    public void setInsuranceImage(MultipartFile insuranceImage) {
        this.insuranceImage = insuranceImage;
    }

    public MultipartFile getPollutionCertificateImage() {
        return pollutionCertificateImage;
    }

    public void setPollutionCertificateImage(MultipartFile pollutionCertificateImage) {
        this.pollutionCertificateImage = pollutionCertificateImage;
    }
}
