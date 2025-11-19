package com.dztech.auth.service;

import com.dztech.auth.dto.AdminDriverDetailResponse;
import com.dztech.auth.dto.AdminDriverListItem;
import com.dztech.auth.dto.AdminDriverListResponse;
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

    public AdminDriverManagementService(
            DriverProfileRepository driverProfileRepository,
            DriverFieldVerificationRepository driverFieldVerificationRepository,
            DriverVehicleRepository driverVehicleRepository) {
        this.driverProfileRepository = driverProfileRepository;
        this.driverFieldVerificationRepository = driverFieldVerificationRepository;
        this.driverVehicleRepository = driverVehicleRepository;
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
        List<DriverFieldVerification> verifications = driverFieldVerificationRepository.findByDriverId(driverId);

        DriverProfileDetailView profileView = toProfileDetailView(profile);
        List<DriverVehicleDetailView> vehicleViews = vehicles.stream().map(this::toVehicleDetailView).toList();
        List<DriverFieldVerificationView> verificationViews = toFieldViews(verifications);

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

        DriverProfileStatus requestedStatus = request.overallStatus();
        if (requestedStatus != null && profile.getStatus() != requestedStatus) {
            profile.setStatus(requestedStatus);
            driverProfileRepository.save(profile);
        }

        return toFieldView(saved);
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
                toDocument("profilePhoto", profile.getProfilePhoto(), profile.getProfilePhotoContentType()),
                toDocument("licenseFront", profile.getLicenseFront(), profile.getLicenseFrontContentType()),
                toDocument("licenseBack", profile.getLicenseBack(), profile.getLicenseBackContentType()),
                toDocument("govIdFront", profile.getGovIdFront(), profile.getGovIdFrontContentType()),
                toDocument("govIdBack", profile.getGovIdBack(), profile.getGovIdBackContentType()));
    }

    private DriverVehicleDetailView toVehicleDetailView(DriverVehicle vehicle) {
        return new DriverVehicleDetailView(
                vehicle.getId(),
                vehicle.getVehicleNumber(),
                vehicle.getVehicleType() == null ? null : vehicle.getVehicleType().name().toLowerCase(Locale.ROOT),
                vehicle.getRcNumber(),
                vehicle.getInsuranceExpiryDate(),
                vehicle.getBrand(),
                vehicle.getModel(),
                toDocument("rc", vehicle.getRcImage(), vehicle.getRcImageContentType()),
                toDocument("insurance", vehicle.getInsuranceImage(), vehicle.getInsuranceImageContentType()),
                toDocument("pollution", vehicle.getPollutionCertificateImage(), vehicle.getPollutionCertificateImageContentType()));
    }

    private DriverDocumentView toDocument(String label, byte[] data, String contentType) {
        if (data == null || data.length == 0) {
            return null;
        }
        String encoded = Base64.getEncoder().encodeToString(data);
        return new DriverDocumentView(label, contentType, encoded);
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
