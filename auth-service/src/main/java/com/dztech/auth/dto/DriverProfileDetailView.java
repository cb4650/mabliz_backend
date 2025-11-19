package com.dztech.auth.dto;

import java.time.LocalDate;

public record DriverProfileDetailView(
        Long driverId,
        String fullName,
        LocalDate dateOfBirth,
        String gender,
        String email,
        String phone,
        String emergencyContactName,
        String emergencyContactNumber,
        String permanentAddress,
        String languages,
        String licenseNumber,
        String licenseType,
        Integer experienceYears,
        String govIdType,
        String govIdNumber,
        DriverDocumentView profilePhoto,
        DriverDocumentView licenseFront,
        DriverDocumentView licenseBack,
        DriverDocumentView govIdFront,
        DriverDocumentView govIdBack) {}
