package com.dztech.rayder.dto;

import com.dztech.rayder.model.VehicleFuelType;
import java.time.LocalDate;
import org.springframework.web.multipart.MultipartFile;

public record OtpVerificationRequest(
        MultipartFile carImage1,
        MultipartFile carImage2,
        MultipartFile carImage3,
        MultipartFile carImage4,
        MultipartFile carImage5,
        MultipartFile carImage6,
        MultipartFile selfieImage,
        VehicleFuelType fuelType,
        String year,
        String policyNo,
        LocalDate startDate,
        LocalDate expiryDate,
        String otp) {
}
