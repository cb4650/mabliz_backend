package com.dztech.auth.service;

import com.dztech.auth.dto.DriverVehicleCreateRequest;
import com.dztech.auth.dto.DriverVehicleView;
import com.dztech.auth.exception.ResourceNotFoundException;
import com.dztech.auth.model.DriverVehicle;
import com.dztech.auth.model.VehicleType;
import com.dztech.auth.repository.DriverProfileRepository;
import com.dztech.auth.repository.DriverVehicleRepository;
import com.dztech.auth.storage.DocumentPathBuilder;
import com.dztech.auth.storage.DocumentStorageService;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DriverVehicleService {

    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;
    private static final List<DateTimeFormatter> INSURANCE_DATE_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("dd-MM-uuuu", Locale.US), DateTimeFormatter.ISO_DATE);

    private final DriverVehicleRepository driverVehicleRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final DocumentStorageService documentStorageService;

    public DriverVehicleService(
            DriverVehicleRepository driverVehicleRepository,
            DriverProfileRepository driverProfileRepository,
            DocumentStorageService documentStorageService) {
        this.driverVehicleRepository = driverVehicleRepository;
        this.driverProfileRepository = driverProfileRepository;
        this.documentStorageService = documentStorageService;
    }

    @Transactional
    public DriverVehicleView addVehicle(Long userId, DriverVehicleCreateRequest request) {
        if (!driverProfileRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Driver profile not found");
        }

        String vehicleNumber = normalizeIdentifier(request.getVehicleNumber());
        if (driverVehicleRepository.existsByVehicleNumberIgnoreCase(vehicleNumber)) {
            throw new IllegalArgumentException("vehicleNumber is already registered");
        }

        VehicleType vehicleType = VehicleType.fromString(request.getVehicleType());
        LocalDate insuranceExpiryDate = parseInsuranceExpiry(request.getInsuranceExpiryDate());
        String rcImageObject =
                uploadVehicleDocument(userId, vehicleNumber, request.getRcImage(), "rc-image");
        String insuranceImageObject =
                uploadVehicleDocument(userId, vehicleNumber, request.getInsuranceImage(), "insurance-image");
        String pollutionCertificateImageObject =
                uploadVehicleDocument(userId, vehicleNumber, request.getPollutionCertificateImage(), "pollution-certificate-image");

        DriverVehicle toSave = DriverVehicle.builder()
                .userId(userId)
                .vehicleNumber(vehicleNumber)
                .vehicleType(vehicleType)
                .rcNumber(normalizeIdentifier(request.getRcNumber()))
                .rcImageObject(rcImageObject)
                .rcImageContentType(request.getRcImage().getContentType())
                .insuranceExpiryDate(insuranceExpiryDate)
                .insuranceImageObject(insuranceImageObject)
                .insuranceImageContentType(request.getInsuranceImage().getContentType())
                .pollutionCertificateImageObject(pollutionCertificateImageObject)
                .pollutionCertificateImageContentType(request.getPollutionCertificateImage().getContentType())
                .brand(normalize(request.getBrand()))
                .model(normalize(request.getModel()))
                .build();

        DriverVehicle saved = driverVehicleRepository.save(toSave);
        return toView(saved);
    }

    private DriverVehicleView toView(DriverVehicle vehicle) {
        return new DriverVehicleView(
                vehicle.getId(),
                vehicle.getVehicleNumber(),
                vehicle.getVehicleType().name().toLowerCase(Locale.ROOT),
                vehicle.getRcNumber(),
                vehicle.getInsuranceExpiryDate(),
                vehicle.getBrand(),
                vehicle.getModel());
    }

    private LocalDate parseInsuranceExpiry(String dateValue) {
        if (!StringUtils.hasText(dateValue)) {
            throw new IllegalArgumentException("insuranceExpiryDate is required");
        }
        String trimmed = dateValue.trim();
        for (DateTimeFormatter formatter : INSURANCE_DATE_FORMATTERS) {
            try {
                return LocalDate.parse(trimmed, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        throw new IllegalArgumentException("insuranceExpiryDate must be in DD-MM-YYYY format");
    }

    private String uploadVehicleDocument(Long userId, String vehicleNumber, MultipartFile file, String label) {
        validateImage(file, label);
        String objectName = DocumentPathBuilder.vehicleDocument(userId, vehicleNumber, label);
        try (InputStream inputStream = file.getInputStream()) {
            documentStorageService.upload(objectName, inputStream, file.getSize(), file.getContentType());
            return objectName;
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to read uploaded file for " + label, ex);
        }
    }

    private void validateImage(MultipartFile file, String fieldName) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        if (file.getSize() > MAX_IMAGE_BYTES) {
            throw new IllegalArgumentException(fieldName + " must be 5 MB or smaller");
        }
        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new IllegalArgumentException(fieldName + " must be an image file");
        }
    }

    private String normalizeIdentifier(String value) {
        return normalize(value).toUpperCase(Locale.ROOT);
    }

    private String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException("Provided value must not be blank");
        }
        return value.trim();
    }
}
