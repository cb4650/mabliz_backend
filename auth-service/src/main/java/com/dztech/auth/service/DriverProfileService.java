package com.dztech.auth.service;

import com.dztech.auth.dto.DriverDocumentView;
import com.dztech.auth.dto.DriverFieldVerificationView;
import com.dztech.auth.dto.DriverProfileUpdateForm;
import com.dztech.auth.dto.DriverProfileView;
import com.dztech.auth.exception.ResourceNotFoundException;
import com.dztech.auth.model.DriverFieldVerification;
import com.dztech.auth.model.DriverFieldVerificationStatus;
import com.dztech.auth.model.DriverProfile;
import com.dztech.auth.model.UserProfile;
import com.dztech.auth.repository.DriverFieldVerificationRepository;
import com.dztech.auth.repository.DriverProfileRepository;
import com.dztech.auth.repository.UserProfileRepository;
import com.dztech.auth.security.DocumentTokenService;
import com.dztech.auth.storage.DocumentPathBuilder;
import com.dztech.auth.storage.DocumentStorageService;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DriverProfileService {

    private static final DateTimeFormatter DOB_FORMATTER = DateTimeFormatter.ISO_DATE;
    private static final long MAX_IMAGE_BYTES = 50L * 1024 * 1024;

    private final DriverProfileRepository driverProfileRepository;
    private final DriverEmailOtpService driverEmailOtpService;
    private final UserProfileRepository userProfileRepository;
    private final DriverFieldVerificationRepository driverFieldVerificationRepository;
    private final DocumentStorageService documentStorageService;
    private final DriverDocumentUrlBuilder driverDocumentUrlBuilder;
    private final DocumentTokenService documentTokenService;

    public DriverProfileService(
            DriverProfileRepository driverProfileRepository,
            DriverEmailOtpService driverEmailOtpService,
            UserProfileRepository userProfileRepository,
            DriverFieldVerificationRepository driverFieldVerificationRepository,
            DocumentStorageService documentStorageService,
            DriverDocumentUrlBuilder driverDocumentUrlBuilder,
            DocumentTokenService documentTokenService) {
        this.driverProfileRepository = driverProfileRepository;
        this.driverEmailOtpService = driverEmailOtpService;
        this.userProfileRepository = userProfileRepository;
        this.driverFieldVerificationRepository = driverFieldVerificationRepository;
        this.documentStorageService = documentStorageService;
        this.driverDocumentUrlBuilder = driverDocumentUrlBuilder;
        this.documentTokenService = documentTokenService;
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
        if (StringUtils.hasText(form.getDob())) {
            profile.setDob(form.getDob().trim());
        }
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
        if (StringUtils.hasText(form.getCurrentAddress())) {
            profile.setCurrentAddress(form.getCurrentAddress().trim());
        }
        if (StringUtils.hasText(form.getMotherTongue())) {
            profile.setMotherTongue(form.getMotherTongue().trim());
        }
        if (StringUtils.hasText(form.getRelationship())) {
            profile.setRelationship(form.getRelationship().trim());
        }
        if (form.getLanguages() != null && !form.getLanguages().isEmpty()) {
            profile.setLanguages(form.getLanguages());
        }
        if (StringUtils.hasText(form.getLicenseNumber())) {
            profile.setLicenseNumber(form.getLicenseNumber().trim());
        }
        if (form.getLicenseType() != null && !form.getLicenseType().isEmpty()) {
            profile.setLicenseType(form.getLicenseType());
        }
        if (StringUtils.hasText(form.getBatch())) {
            profile.setBatch(form.getBatch().trim());
        }
        if (StringUtils.hasText(form.getExpiryDate())) {
            profile.setExpiryDate(form.getExpiryDate().trim());
        }
        if (form.getTransmission() != null && !form.getTransmission().isEmpty()) {
            profile.setTransmission(form.getTransmission());
        }
        if (StringUtils.hasText(form.getExperience())) {
            profile.setExperience(form.getExperience().trim());
        }
        if (StringUtils.hasText(form.getGovIdType())) {
            profile.setGovIdType(form.getGovIdType().trim());
        }
        if (StringUtils.hasText(form.getGovIdNumber())) {
            profile.setGovIdNumber(form.getGovIdNumber().trim());
        }
        if (StringUtils.hasText(form.getExpiryDateKyc())) {
            profile.setExpiryDateKyc(form.getExpiryDateKyc().trim());
        }
        if (StringUtils.hasText(form.getBloodGroup())) {
            profile.setBloodGroup(form.getBloodGroup().trim());
        }
        if (StringUtils.hasText(form.getQualification())) {
            profile.setQualification(form.getQualification().trim());
        }
        if (StringUtils.hasText(form.getBatchNumber())) {
            profile.setBatchNumber(form.getBatchNumber().trim());
        }
        if (StringUtils.hasText(form.getBatchExpiryDate())) {
            profile.setBatchExpiryDate(form.getBatchExpiryDate().trim());
        }
        if (StringUtils.hasText(form.getFatherName())) {
            profile.setFatherName(form.getFatherName().trim());
        }

        if (form.getProfilePhoto() != null && !form.getProfilePhoto().isEmpty()) {
            DocumentUpload upload = uploadProfileDocument(userId, form.getProfilePhoto(), "profile-photo");
            profile.setProfilePhotoObject(upload.objectName());
            profile.setProfilePhoto(null);
            profile.setProfilePhotoContentType(upload.contentType());
        }
        if (form.getLicenseFront() != null && !form.getLicenseFront().isEmpty()) {
            DocumentUpload upload = uploadProfileDocument(userId, form.getLicenseFront(), "license-front");
            profile.setLicenseFrontObject(upload.objectName());
            profile.setLicenseFront(null);
            profile.setLicenseFrontContentType(upload.contentType());
        }
        if (form.getLicenseBack() != null && !form.getLicenseBack().isEmpty()) {
            DocumentUpload upload = uploadProfileDocument(userId, form.getLicenseBack(), "license-back");
            profile.setLicenseBackObject(upload.objectName());
            profile.setLicenseBack(null);
            profile.setLicenseBackContentType(upload.contentType());
        }
        if (form.getGovIdFront() != null && !form.getGovIdFront().isEmpty()) {
            DocumentUpload upload = uploadProfileDocument(userId, form.getGovIdFront(), "gov-id-front");
            profile.setGovIdFrontObject(upload.objectName());
            profile.setGovIdFront(null);
            profile.setGovIdFrontContentType(upload.contentType());
        }
        if (form.getGovIdBack() != null && !form.getGovIdBack().isEmpty()) {
            DocumentUpload upload = uploadProfileDocument(userId, form.getGovIdBack(), "gov-id-back");
            profile.setGovIdBackObject(upload.objectName());
            profile.setGovIdBack(null);
            profile.setGovIdBackContentType(upload.contentType());
        }

        DriverProfile updated = driverProfileRepository.save(profile);

        if (emailChanged) {
            driverEmailOtpService.sendOtp(userId, updated.getEmail(), updated.getFullName());
        }
        return toView(updated);
    }

    private DocumentUpload uploadProfileDocument(Long userId, MultipartFile file, String label) {
        String contentType = validateImage(file, label);
        String objectName = DocumentPathBuilder.profileDocument(userId, label);
        try (InputStream inputStream = file.getInputStream()) {
            documentStorageService.upload(objectName, inputStream, file.getSize(), contentType);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Failed to read uploaded file for " + label, ex);
        }
        return new DocumentUpload(objectName, contentType);
    }

    private DriverProfileView toView(DriverProfile profile) {
        boolean emailVerified = userProfileRepository
                .findByUserId(profile.getUserId())
                .map(UserProfile::isEmailVerified)
                .orElse(false);

        return new DriverProfileView(
                profile.getUserId(),
                profile.getFullName(),
                profile.getDob(),
                profile.getGender(),
                profile.getEmail(),
                emailVerified,
                profile.getPhone(),
                profile.getEmergencyContactName(),
                profile.getEmergencyContactNumber(),
                profile.getPermanentAddress(),
                profile.getCurrentAddress(),
                profile.getMotherTongue(),
                profile.getRelationship(),
                profile.getLanguages(),
                profile.getLicenseNumber(),
                profile.getLicenseType(),
                profile.getBatch(),
                profile.getExpiryDate(),
                profile.getTransmission(),
                profile.getExperience(),
                profile.getGovIdType(),
                profile.getGovIdNumber(),
                profile.getExpiryDateKyc(),
                profile.getBloodGroup(),
                profile.getQualification(),
                profile.getBatchNumber(),
                profile.getBatchExpiryDate(),
                profile.getFatherName(),
                toDocument(
                        profile.getUserId(),
                        "profilePhoto",
                        profile.getProfilePhotoObject(),
                        profile.getProfilePhoto(),
                        profile.getProfilePhotoContentType()),
                toDocument(
                        profile.getUserId(),
                        "licenseFront",
                        profile.getLicenseFrontObject(),
                        profile.getLicenseFront(),
                        profile.getLicenseFrontContentType()),
                toDocument(
                        profile.getUserId(),
                        "licenseBack",
                        profile.getLicenseBackObject(),
                        profile.getLicenseBack(),
                        profile.getLicenseBackContentType()),
                toDocument(
                        profile.getUserId(),
                        "govIdFront",
                        profile.getGovIdFrontObject(),
                        profile.getGovIdFront(),
                        profile.getGovIdFrontContentType()),
                toDocument(
                        profile.getUserId(),
                        "govIdBack",
                        profile.getGovIdBackObject(),
                        profile.getGovIdBack(),
                        profile.getGovIdBackContentType()),
                profile.getStatus(),
                getFieldVerifications(profile.getUserId()));
    }

    private DriverDocumentView toDocument(
            Long driverId, String label, String objectName, byte[] legacyData, String contentType) {
        if (!StringUtils.hasText(objectName) && (legacyData == null || legacyData.length == 0)) {
            return null;
        }

        String token = documentTokenService.issueProfileDocumentToken(driverId, label);
        String url = driverDocumentUrlBuilder.profileDocument(driverId, label, token);

        String encoded = null;
        if (legacyData != null && legacyData.length > 0) {
            encoded = Base64.getEncoder().encodeToString(legacyData);
        }

        return new DriverDocumentView(label, contentType, encoded, url);
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

    private String validateImage(MultipartFile file, String fieldName) {
        if (file.getSize() > MAX_IMAGE_BYTES) {
            throw new IllegalArgumentException(fieldName + " must be 5 MB or smaller");
        }
        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !contentType.toLowerCase().startsWith("image/")) {
            throw new IllegalArgumentException(fieldName + " must be an image file");
        }
        return contentType;
    }

    private List<DriverFieldVerificationView> getFieldVerifications(Long driverId) {
        List<DriverFieldVerification> existingVerifications = driverFieldVerificationRepository.findByDriverId(driverId);

        Map<String, DriverFieldVerification> verificationMap = existingVerifications.stream()
                .collect(Collectors.toMap(DriverFieldVerification::getFieldName, v -> v));

        return DriverFieldVerificationFields.all().stream()
                .map(fieldName -> {
                    DriverFieldVerification verification = verificationMap.get(fieldName);
                    if (verification != null) {
                        return toFieldView(verification);
                    }
                    return new DriverFieldVerificationView(
                            fieldName,
                            DriverFieldVerificationStatus.PENDING,
                            null,
                            null,
                            null);
                })
                .sorted(Comparator.comparing(DriverFieldVerificationView::fieldName))
                .toList();
    }

    private DriverFieldVerificationView toFieldView(DriverFieldVerification verification) {
        return new DriverFieldVerificationView(
                verification.getFieldName(),
                verification.getStatus(),
                verification.getNotes(),
                verification.getVerifiedByAdminId(),
                verification.getVerifiedAt());
    }

    private record DocumentUpload(String objectName, String contentType) {}
}
