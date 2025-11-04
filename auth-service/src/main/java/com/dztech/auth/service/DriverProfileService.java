package com.dztech.auth.service;

import com.dztech.auth.dto.DriverProfileUpdateForm;
import com.dztech.auth.dto.DriverProfileView;
import com.dztech.auth.exception.ResourceNotFoundException;
import com.dztech.auth.model.DriverProfile;
import com.dztech.auth.repository.DriverProfileRepository;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DriverProfileService {

    private static final DateTimeFormatter DOB_FORMATTER = DateTimeFormatter.ISO_DATE;
    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;

    private final DriverProfileRepository driverProfileRepository;
    private final DriverEmailOtpService driverEmailOtpService;

    public DriverProfileService(
            DriverProfileRepository driverProfileRepository, DriverEmailOtpService driverEmailOtpService) {
        this.driverProfileRepository = driverProfileRepository;
        this.driverEmailOtpService = driverEmailOtpService;
    }

    @Transactional(readOnly = true)
    public DriverProfileView getProfile(Long userId) {
        DriverProfile profile = driverProfileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found"));
        return toView(profile);
    }

    @Transactional
    public DriverProfileView updateProfile(Long userId, DriverProfileUpdateForm form) {
        DriverProfile profile = driverProfileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found"));

        if (StringUtils.hasText(form.getFullName())) {
            profile.setFullName(form.getFullName().trim());
        }
        boolean emailChanged = false;
        if (StringUtils.hasText(form.getEmail())) {
            String normalizedEmail = form.getEmail().trim().toLowerCase();
            driverProfileRepository.findByEmail(normalizedEmail)
                    .filter(existing -> !existing.getUserId().equals(userId))
                    .ifPresent(existing -> {
                        throw new IllegalArgumentException("Email already in use");
                    });
            String currentEmail = profile.getEmail();
            if (currentEmail == null || !currentEmail.equalsIgnoreCase(normalizedEmail)) {
                profile.setEmail(normalizedEmail);
                emailChanged = true;
            }
        }
        profile.setDateOfBirth(parseDate(form.getDob(), profile.getDateOfBirth()));
        if (StringUtils.hasText(form.getGender())) {
            profile.setGender(form.getGender().trim());
        }
        if (StringUtils.hasText(form.getPhone())) {
            profile.setPhone(form.getPhone().trim());
        }
        if (StringUtils.hasText(form.getEmergencyContactName())) {
            profile.setEmergencyContactName(form.getEmergencyContactName().trim());
        }
        if (StringUtils.hasText(form.getEmergencyContactNumber())) {
            profile.setEmergencyContactNumber(form.getEmergencyContactNumber().trim());
        }
        if (StringUtils.hasText(form.getPermanentAddress())) {
            profile.setPermanentAddress(form.getPermanentAddress().trim());
        }
        if (StringUtils.hasText(form.getLanguages())) {
            profile.setLanguages(form.getLanguages().trim());
        }
        if (StringUtils.hasText(form.getLicenseNumber())) {
            profile.setLicenseNumber(form.getLicenseNumber().trim());
        }
        if (StringUtils.hasText(form.getLicenseType())) {
            profile.setLicenseType(form.getLicenseType().trim());
        }
        if (StringUtils.hasText(form.getExperience())) {
            profile.setExperienceYears(parseExperience(form.getExperience(), profile.getExperienceYears()));
        }
        if (StringUtils.hasText(form.getGovIdType())) {
            profile.setGovIdType(form.getGovIdType().trim());
        }
        if (StringUtils.hasText(form.getGovIdNumber())) {
            profile.setGovIdNumber(form.getGovIdNumber().trim());
        }

        if (form.getProfilePhoto() != null && !form.getProfilePhoto().isEmpty()) {
            ImagePayload upload = readImage(form.getProfilePhoto(), "profilePhoto");
            profile.setProfilePhoto(upload.data());
            profile.setProfilePhotoContentType(upload.contentType());
        }
        if (form.getLicenseFront() != null && !form.getLicenseFront().isEmpty()) {
            ImagePayload upload = readImage(form.getLicenseFront(), "licenseFront");
            profile.setLicenseFront(upload.data());
            profile.setLicenseFrontContentType(upload.contentType());
        }
        if (form.getLicenseBack() != null && !form.getLicenseBack().isEmpty()) {
            ImagePayload upload = readImage(form.getLicenseBack(), "licenseBack");
            profile.setLicenseBack(upload.data());
            profile.setLicenseBackContentType(upload.contentType());
        }
        if (form.getGovIdFront() != null && !form.getGovIdFront().isEmpty()) {
            ImagePayload upload = readImage(form.getGovIdFront(), "govIdFront");
            profile.setGovIdFront(upload.data());
            profile.setGovIdFrontContentType(upload.contentType());
        }
        if (form.getGovIdBack() != null && !form.getGovIdBack().isEmpty()) {
            ImagePayload upload = readImage(form.getGovIdBack(), "govIdBack");
            profile.setGovIdBack(upload.data());
            profile.setGovIdBackContentType(upload.contentType());
        }

        DriverProfile updated = driverProfileRepository.save(profile);

        if (emailChanged) {
            driverEmailOtpService.sendOtp(userId, updated.getEmail(), updated.getFullName());
        }
        return toView(updated);
    }

    private ImagePayload readImage(MultipartFile file, String fieldName) {
        if (file.getSize() > MAX_IMAGE_BYTES) {
            throw new IllegalArgumentException(fieldName + " must be 5 MB or smaller");
        }
        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !contentType.toLowerCase().startsWith("image/")) {
            throw new IllegalArgumentException(fieldName + " must be an image file");
        }
        try {
            return new ImagePayload(file.getBytes(), contentType);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Failed to read uploaded file for " + fieldName, ex);
        }
    }

    private DriverProfileView toView(DriverProfile profile) {
        return new DriverProfileView(
                profile.getUserId(),
                profile.getFullName(),
                profile.getDateOfBirth(),
                profile.getGender(),
                profile.getEmail(),
                profile.getPhone(),
                profile.getEmergencyContactName(),
                profile.getEmergencyContactNumber(),
                profile.getPermanentAddress(),
                profile.getLanguages(),
                profile.getLicenseNumber(),
                profile.getLicenseType(),
                profile.getExperienceYears(),
                profile.getGovIdType(),
                profile.getGovIdNumber());
    }

    private LocalDate parseDate(String dob, LocalDate fallback) {
        if (!StringUtils.hasText(dob)) {
            return fallback;
        }
        try {
            return LocalDate.parse(dob.trim(), DOB_FORMATTER);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid date format for dob. Expected yyyy-MM-dd", ex);
        }
    }

    private Integer parseExperience(String experience, Integer fallback) {
        if (!StringUtils.hasText(experience)) {
            return fallback;
        }
        try {
            return Integer.parseInt(experience.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Experience must be a valid number", ex);
        }
    }

    private record ImagePayload(byte[] data, String contentType) {}
}
