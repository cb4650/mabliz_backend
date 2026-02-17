package com.dztech.rayder.dto;

import java.time.LocalDate;
import org.springframework.web.multipart.MultipartFile;

public record OtpVerificationRequest(
        MultipartFile carImage1,
        MultipartFile carImage2,
        MultipartFile carImage3,
        MultipartFile carImage4,
        MultipartFile carImage5,
        MultipartFile carImage6,
        String vehicleNo,
        String insuranceNo,
        LocalDate insuranceExpiry,
        MultipartFile insurancePhoto,
        String otp) {
}
