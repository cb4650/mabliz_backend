package com.dztech.auth.dto;

import com.dztech.auth.model.DriverProfileStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record DriverProfileView(
        Long userId,
        String fullName,
        String dob,
        String gender,
        String email,
        @JsonProperty("isEmailVerified")
        boolean emailVerified,
        String phone,
        String emergencyContactName,
        String emergencyContactNumber,
        String permanentAddress,
        String currentAddress,
        String motherTongue,
        String relationship,
        List<String> languages,
        String licenseNumber,
        List<String> licenseType,
        String batch,
        String expiryDate,
        List<String> transmission,
        String experience,
        String govIdType,
        String govIdNumber,
        String expiryDateKyc,
        String bloodGroup,
        String qualification,
        String batchNumber,
        String batchExpiryDate,
        String fatherName,
        DriverDocumentView profilePhoto,
        DriverDocumentView licenseFront,
        DriverDocumentView licenseBack,
        DriverDocumentView govIdFront,
        DriverDocumentView govIdBack,
        DriverProfileStatus status,
        List<DriverFieldVerificationView> fieldVerifications) {
}
