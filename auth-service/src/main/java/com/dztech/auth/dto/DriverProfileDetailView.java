package com.dztech.auth.dto;

import java.util.List;

public record DriverProfileDetailView(
        Long driverId,
        String fullName,
        String dob,
        String gender,
        String email,
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
        DriverDocumentView profilePhoto,
        DriverDocumentView licenseFront,
        DriverDocumentView licenseBack,
        DriverDocumentView govIdFront,
        DriverDocumentView govIdBack) {}
