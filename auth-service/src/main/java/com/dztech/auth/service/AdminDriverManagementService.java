package com.dztech.auth.service;

import com.dztech.auth.dto.AdminDriverDetailResponse;
import com.dztech.auth.dto.AdminDriverListItem;
import com.dztech.auth.dto.AdminDriverListResponse;
import com.dztech.auth.dto.DriverBulkFieldVerificationRequest;
import com.dztech.auth.dto.DriverBulkFieldVerificationResponse;
import com.dztech.auth.dto.DriverDocumentView;
import com.dztech.auth.dto.DriverFieldVerificationRequest;
import com.dztech.auth.dto.DriverFieldVerificationView;
import com.dztech.auth.dto.DriverProfileDetailView;
import com.dztech.auth.dto.DriverVehicleDetailView;
import com.dztech.auth.exception.ResourceNotFoundException;
import com.dztech.auth.model.DriverFieldVerification;
import com.dztech.auth.model.DriverFieldVerificationStatus;
import com.dztech.auth.model.DriverProfile;
import com.dztech.auth.model.DriverProfileStatus;
import com.dztech.auth.model.DriverVehicle;
import com.dztech.auth.repository.DriverFieldVerificationRepository;
import com.dztech.auth.repository.DriverProfileRepository;
import com.dztech.auth.repository.DriverVehicleRepository;
import com.dztech.auth.security.DocumentTokenService;
import com.dztech.auth.storage.DocumentStorageService;
import java.time.Instant;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AdminDriverManagementService {

    private final DriverProfileRepository driverProfileRepository;
    private final DriverFieldVerificationRepository driverFieldVerificationRepository;
    private final DriverVehicleRepository driverVehicleRepository;
    private final DocumentStorageService documentStorageService;
    private final DriverDocumentUrlBuilder driverDocumentUrlBuilder;
    private final DocumentTokenService documentTokenService;

    public AdminDriverManagementService(
            DriverProfileRepository driverProfileRepository,
            DriverFieldVerificationRepository driverFieldVerificationRepository,
            DriverVehicleRepository driverVehicleRepository,
            DocumentStorageService documentStorageService,
            DriverDocumentUrlBuilder driverDocumentUrlBuilder,
            DocumentTokenService documentTokenService) {
        this.driverProfileRepository = driverProfileRepository;
        this.driverFieldVerificationRepository = driverFieldVerificationRepository;
        this.driverVehicleRepository = driverVehicleRepository;
        this.documentStorageService = documentStorageService;
        this.driverDocumentUrlBuilder = driverDocumentUrlBuilder;
        this.documentTokenService = documentTokenService;
    }

    @Transactional(readOnly = true)
    public AdminDriverListResponse listDrivers() {
        List<DriverProfile> profiles =
                driverProfileRepository.findAll(Sort.by(Sort.Direction.DESC, "updatedAt"));
        if (profiles.isEmpty()) {
            return new AdminDriverListResponse(true, List.of());
        }

        List<Long> driverIds = profiles.stream().map(DriverProfile::getUserId).toList();
        Map<Long, List<DriverFieldVerification>> verificationMap = driverFieldVerificationRepository
                .findByDriverIdIn(driverIds)
                .stream()
                .collect(Collectors.groupingBy(DriverFieldVerification::getDriverId));

        List<AdminDriverListItem> drivers = profiles.stream()
                .map(profile -> new AdminDriverListItem(
                        profile.getUserId(),
                        profile.getFullName(),
                        profile.getEmail(),
                        profile.getPhone(),
                        profile.getStatus(),
                        profile.getUpdatedAt(),
                        toFieldViews(verificationMap.get(profile.getUserId()))))
                .toList();

        return new AdminDriverListResponse(true, drivers);
    }

    @Transactional(readOnly = true)
    public AdminDriverDetailResponse getDriverDetail(Long driverId) {
        DriverProfile profile = driverProfileRepository
                .findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found"));

        List<DriverVehicle> vehicles = driverVehicleRepository.findByUserIdOrderByCreatedAtDesc(driverId);
        List<DriverFieldVerificationView> verificationViews = getAllFieldVerifications(driverId);

        DriverProfileDetailView profileView = toProfileDetailView(profile);
        List<DriverVehicleDetailView> vehicleViews = vehicles.stream().map(this::toVehicleDetailView).toList();

        return new AdminDriverDetailResponse(
                true, profile.getUserId(), profile.getStatus(), profileView, vehicleViews, verificationViews);
    }

    @Transactional
    public DriverFieldVerificationView verifyField(
            Long adminId, Long driverId, DriverFieldVerificationRequest request) {
        DriverProfile profile = driverProfileRepository
                .findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found"));

        String fieldName = normalizeFieldName(request.fieldName());
        DriverFieldVerificationStatus status = request.status();
        Instant now = Instant.now();

        DriverFieldVerification verification = driverFieldVerificationRepository
                .findByDriverIdAndFieldNameIgnoreCase(driverId, fieldName)
                .orElseGet(() -> DriverFieldVerification.builder()
                        .driverId(driverId)
                        .fieldName(fieldName)
                        .status(DriverFieldVerificationStatus.PENDING)
                        .build());

        verification.setFieldName(fieldName);
        verification.setStatus(status);
        verification.setNotes(normalizeNotes(request.notes()));
        verification.setVerifiedByAdminId(adminId);
        verification.setVerifiedAt(status == DriverFieldVerificationStatus.PENDING ? null : now);

        DriverFieldVerification saved = driverFieldVerificationRepository.save(verification);

        // Update driver profile status based on all field verifications
        updateDriverProfileStatus(profile);

        return toFieldView(saved);
    }

    @Transactional
    public DriverBulkFieldVerificationResponse bulkVerifyFields(
            Long adminId, DriverBulkFieldVerificationRequest request) {
        Long driverId = request.driverId();

        DriverProfile profile = driverProfileRepository
                .findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found"));

        List<DriverFieldVerification> savedVerifications = request.verifications().stream()
                .map(fieldReq -> {
                    String fieldName = normalizeFieldName(fieldReq.fieldName());
                    DriverFieldVerificationStatus status = fieldReq.status();
                    Instant now = Instant.now();

                    DriverFieldVerification verification = driverFieldVerificationRepository
                            .findByDriverIdAndFieldNameIgnoreCase(driverId, fieldName)
                            .orElseGet(() -> DriverFieldVerification.builder()
                                    .driverId(driverId)
                                    .fieldName(fieldName)
                                    .status(DriverFieldVerificationStatus.PENDING)
                                    .build());

                    verification.setFieldName(fieldName);
                    verification.setStatus(status);
                    verification.setNotes(normalizeNotes(fieldReq.notes()));
                    verification.setVerifiedByAdminId(adminId);
                    verification.setVerifiedAt(status == DriverFieldVerificationStatus.PENDING ? null : now);

                    return driverFieldVerificationRepository.save(verification);
                })
                .toList();

        // Update driver profile status based on all field verifications
        updateDriverProfileStatus(profile);

        List<DriverFieldVerificationView> verificationViews = savedVerifications.stream()
                .map(this::toFieldView)
                .sorted(Comparator.comparing(DriverFieldVerificationView::fieldName))
                .toList();

        return new DriverBulkFieldVerificationResponse(true, "Driver fields verified successfully", verificationViews);
    }

    private List<DriverFieldVerificationView> toFieldViews(List<DriverFieldVerification> verifications) {
        if (verifications == null || verifications.isEmpty()) {
            return List.of();
        }
        return verifications.stream()
                .map(this::toFieldView)
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

    private DriverProfileDetailView toProfileDetailView(DriverProfile profile) {
        return new DriverProfileDetailView(
                profile.getUserId(),
                profile.getFullName(),
                profile.getDob(),
                profile.getGender(),
                profile.getEmail(),
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
                        profile.getProfilePhotoContentType(),
                        true),
                toDocument(
                        profile.getUserId(),
                        "licenseFront",
                        profile.getLicenseFrontObject(),
                        profile.getLicenseFront(),
                        profile.getLicenseFrontContentType(),
                        true),
                toDocument(
                        profile.getUserId(),
                        "licenseBack",
                        profile.getLicenseBackObject(),
                        profile.getLicenseBack(),
                        profile.getLicenseBackContentType(),
                        true),
                toDocument(
                        profile.getUserId(),
                        "govIdFront",
                        profile.getGovIdFrontObject(),
                        profile.getGovIdFront(),
                        profile.getGovIdFrontContentType(),
                        true),
                toDocument(
                        profile.getUserId(),
                        "govIdBack",
                        profile.getGovIdBackObject(),
                        profile.getGovIdBack(),
                        profile.getGovIdBackContentType(),
                        true));
    }

    private DriverVehicleDetailView toVehicleDetailView(DriverVehicle vehicle) {
        return new DriverVehicleDetailView(
                vehicle.getId(),
                vehicle.getVehicleNumber(),
                vehicle.getVehicleType() == null ? null : vehicle.getVehicleType().name().toLowerCase(Locale.ROOT),
                vehicle.getManufacturedYear(),
                vehicle.getInsuranceExpiryDate(),
                vehicle.getBrand(),
                vehicle.getModel(),
                toDocument(
                        vehicle.getUserId(),
                        "rc",
                        vehicle.getRcImageObject(),
                        vehicle.getRcImage(),
                        vehicle.getRcImageContentType(),
                        false),
                toDocument(
                        vehicle.getUserId(),
                        "insurance",
                        vehicle.getInsuranceImageObject(),
                        vehicle.getInsuranceImage(),
                        vehicle.getInsuranceImageContentType(),
                        false),
                toDocument(
                        vehicle.getUserId(),
                        "pollution",
                        vehicle.getPollutionCertificateImageObject(),
                        vehicle.getPollutionCertificateImage(),
                        vehicle.getPollutionCertificateImageContentType(),
                        false));
    }

    private DriverDocumentView toDocument(
            Long driverId, String label, String objectName, byte[] legacyData, String contentType, boolean profileDocument) {
        if (!StringUtils.hasText(objectName) && (legacyData == null || legacyData.length == 0)) {
            return null;
        }
        String encoded = null;
        if (legacyData != null && legacyData.length > 0) {
            encoded = Base64.getEncoder().encodeToString(legacyData);
        }

        String url;
        if (profileDocument) {
            String token = documentTokenService.issueProfileDocumentToken(driverId, label);
            url = driverDocumentUrlBuilder.profileDocument(driverId, label, token);
        } else if (StringUtils.hasText(objectName)) {
            url = documentStorageService.getPresignedUrl(objectName).orElse(null);
        } else {
            url = null;
        }
        return new DriverDocumentView(label, contentType, encoded, url);
    }

    private List<DriverFieldVerificationView> getAllFieldVerifications(Long driverId) {
        List<DriverFieldVerification> existingVerifications = driverFieldVerificationRepository.findByDriverId(driverId);

        // Create a map of existing verifications by field name
        Map<String, DriverFieldVerification> verificationMap = existingVerifications.stream()
                .collect(Collectors.toMap(DriverFieldVerification::getFieldName, v -> v));

        // Get all verifiable field names
        List<String> allVerifiableFields = DriverFieldVerificationFields.all();

        // Create verification views for all fields, using existing records or creating pending ones
        return allVerifiableFields.stream()
                .map(fieldName -> {
                    DriverFieldVerification verification = verificationMap.get(fieldName);
                    if (verification != null) {
                        return toFieldView(verification);
                    } else {
                        // Create a pending verification view for fields that don't exist yet
                        return new DriverFieldVerificationView(
                                fieldName,
                                DriverFieldVerificationStatus.PENDING,
                                null,
                                null,
                                null);
                    }
                })
                .sorted(Comparator.comparing(DriverFieldVerificationView::fieldName))
                .toList();
    }

    /**
     * Updates the driver profile status based on field verifications.
     * Logic: If any field is REJECTED -> status becomes REJECTED
     *        If all fields are VERIFIED -> status becomes VERIFIED
     *        If all fields are PENDING -> status becomes PENDING
     */
    private void updateDriverProfileStatus(DriverProfile profile) {
        Long driverId = profile.getUserId();

        // Get all existing field verifications for this driver
        List<DriverFieldVerification> allVerifications = driverFieldVerificationRepository.findByDriverId(driverId);

        // If no verifications exist yet, set to PENDING
        if (allVerifications.isEmpty()) {
            profile.setStatus(DriverProfileStatus.PENDING);
            driverProfileRepository.save(profile);
            return;
        }

        // Create a map for quick lookup
        Map<String, DriverFieldVerificationStatus> verificationStatusMap = allVerifications.stream()
                .collect(Collectors.toMap(DriverFieldVerification::getFieldName, DriverFieldVerification::getStatus));

        // Get all verifiable field names
        List<String> verifiableFields = DriverFieldVerificationFields.all();
        int totalFields = verifiableFields.size();

        // Count statuses
        long rejectedCount = 0;
        long verifiedCount = 0;
        long pendingCount = 0;

        // Check each verifiable field
        for (String fieldName : verifiableFields) {
            DriverFieldVerificationStatus status = verificationStatusMap.get(fieldName);

            // If verification record doesn't exist, treat as PENDING
            if (status == null) {
                status = DriverFieldVerificationStatus.PENDING;
            }

            if (status == DriverFieldVerificationStatus.REJECTED) {
                rejectedCount++;
            } else if (status == DriverFieldVerificationStatus.VERIFIED) {
                verifiedCount++;
            } else if (status == DriverFieldVerificationStatus.PENDING) {
                pendingCount++;
            }
        }

        // Determine new status
        DriverProfileStatus newStatus;
        if (rejectedCount > 0) {
            // If any field is rejected, overall status is BANNED
            newStatus = DriverProfileStatus.BANNED;
        } else if (verifiedCount == totalFields) {
            // If all fields are verified, overall status is VERIFIED
            newStatus = DriverProfileStatus.VERIFIED;
        } else {
            // If there are pending fields, overall status is PENDING
            newStatus = DriverProfileStatus.PENDING;
        }

        // Update profile status only if it changed
        if (profile.getStatus() != newStatus) {
            profile.setStatus(newStatus);
            driverProfileRepository.save(profile);
        }
    }

    private String normalizeFieldName(String rawField) {
        if (!StringUtils.hasText(rawField)) {
            throw new IllegalArgumentException("fieldName is required");
        }
        return rawField.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeNotes(String rawNotes) {
        if (!StringUtils.hasText(rawNotes)) {
            return null;
        }
        String trimmed = rawNotes.trim();
        return trimmed.length() > 255 ? trimmed.substring(0, 255) : trimmed;
    }
}
