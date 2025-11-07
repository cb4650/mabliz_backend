package com.dztech.auth.service;

import com.dztech.auth.dto.DriverVehicleCreateRequest;
import com.dztech.auth.dto.DriverVehicleView;
import com.dztech.auth.exception.ResourceNotFoundException;
import com.dztech.auth.model.DriverVehicle;
import com.dztech.auth.model.VehicleType;
import com.dztech.auth.repository.DriverProfileRepository;
import com.dztech.auth.repository.DriverVehicleRepository;
import java.io.IOException;
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

    public DriverVehicleService(
            DriverVehicleRepository driverVehicleRepository, DriverProfileRepository driverProfileRepository) {
        this.driverVehicleRepository = driverVehicleRepository;
        this.driverProfileRepository = driverProfileRepository;
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

        ImagePayload rcImage = readImage(request.getRcImage(), "rcImage");
        ImagePayload insuranceImage = readImage(request.getInsuranceImage(), "insuranceImage");
        ImagePayload pollutionCertificateImage =
                readImage(request.getPollutionCertificateImage(), "pollutionCertificateImage");

        DriverVehicle toSave = DriverVehicle.builder()
                .userId(userId)
                .vehicleNumber(vehicleNumber)
                .vehicleType(vehicleType)
                .rcNumber(normalizeIdentifier(request.getRcNumber()))
                .rcImage(rcImage.data())
                .rcImageContentType(rcImage.contentType())
                .insuranceExpiryDate(insuranceExpiryDate)
                .insuranceImage(insuranceImage.data())
                .insuranceImageContentType(insuranceImage.contentType())
                .pollutionCertificateImage(pollutionCertificateImage.data())
                .pollutionCertificateImageContentType(pollutionCertificateImage.contentType())
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

    private ImagePayload readImage(MultipartFile file, String fieldName) {
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
        try {
            return new ImagePayload(file.getBytes(), contentType);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to read uploaded file for " + fieldName, ex);
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

    private record ImagePayload(byte[] data, String contentType) {}
}
