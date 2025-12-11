package com.dztech.auth.service;

import com.dztech.auth.exception.ResourceNotFoundException;
import com.dztech.auth.model.DriverVehicle;
import com.dztech.auth.repository.DriverVehicleRepository;
import com.dztech.auth.storage.DocumentStorageService;
import java.util.Locale;
import java.util.function.Function;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class DriverVehicleDocumentAccessService {

    private final DriverVehicleRepository driverVehicleRepository;
    private final DocumentStorageService documentStorageService;

    public DriverVehicleDocumentAccessService(
            DriverVehicleRepository driverVehicleRepository, DocumentStorageService documentStorageService) {
        this.driverVehicleRepository = driverVehicleRepository;
        this.documentStorageService = documentStorageService;
    }

    public DriverDocumentResource getVehicleDocument(
            Long requesterId, boolean requesterIsAdmin, Long vehicleId, String rawLabel) {
        DriverVehicle vehicle = driverVehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver vehicle not found"));

        if (requesterId == null) {
            throw new AccessDeniedException("Authentication is required to access this document");
        }
        if (!requesterIsAdmin && !vehicle.getUserId().equals(requesterId)) {
            throw new AccessDeniedException("You are not allowed to access this document");
        }

        VehicleDocumentSlot slot = VehicleDocumentSlot.fromLabel(rawLabel);
        if (slot == null) {
            throw new ResourceNotFoundException("Document not found");
        }

        DocumentContent content = slot.resolve(vehicle, documentStorageService);
        if (content.data() == null || content.data().length == 0) {
            throw new ResourceNotFoundException("Document not found");
        }

        String contentType = StringUtils.hasText(content.contentType())
                ? content.contentType()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        boolean inlineImage = contentType.toLowerCase(Locale.ROOT).startsWith("image/");

        return new DriverDocumentResource(slot.label(), contentType, inlineImage, content.data());
    }

    private enum VehicleDocumentSlot {
        RC(
                "rc",
                DriverVehicle::getRcImageObject,
                DriverVehicle::getRcImage,
                DriverVehicle::getRcImageContentType),
        INSURANCE(
                "insurance",
                DriverVehicle::getInsuranceImageObject,
                DriverVehicle::getInsuranceImage,
                DriverVehicle::getInsuranceImageContentType),
        POLLUTION(
                "pollution",
                DriverVehicle::getPollutionCertificateImageObject,
                DriverVehicle::getPollutionCertificateImage,
                DriverVehicle::getPollutionCertificateImageContentType);

        private final String label;
        private final Function<DriverVehicle, String> objectNameExtractor;
        private final Function<DriverVehicle, byte[]> legacyDataExtractor;
        private final Function<DriverVehicle, String> contentTypeExtractor;

        VehicleDocumentSlot(
                String label,
                Function<DriverVehicle, String> objectNameExtractor,
                Function<DriverVehicle, byte[]> legacyDataExtractor,
                Function<DriverVehicle, String> contentTypeExtractor) {
            this.label = label;
            this.objectNameExtractor = objectNameExtractor;
            this.legacyDataExtractor = legacyDataExtractor;
            this.contentTypeExtractor = contentTypeExtractor;
        }

        static VehicleDocumentSlot fromLabel(String rawLabel) {
            if (!StringUtils.hasText(rawLabel)) {
                return null;
            }
            String trimmed = rawLabel.trim();
            for (VehicleDocumentSlot slot : values()) {
                if (slot.label.equalsIgnoreCase(trimmed)) {
                    return slot;
                }
            }
            return null;
        }

        DocumentContent resolve(DriverVehicle vehicle, DocumentStorageService storageService) {
            String objectName = objectNameExtractor.apply(vehicle);
            byte[] data = null;
            if (StringUtils.hasText(objectName)) {
                data = storageService.download(objectName).orElse(null);
            }
            if (data == null || data.length == 0) {
                byte[] legacyData = legacyDataExtractor.apply(vehicle);
                if (legacyData != null && legacyData.length > 0) {
                    data = legacyData;
                }
            }
            return new DocumentContent(data, contentTypeExtractor.apply(vehicle));
        }

        String label() {
            return label;
        }
    }

    private record DocumentContent(byte[] data, String contentType) {}

    public record DriverDocumentResource(String label, String contentType, boolean inlineImage, byte[] data) {}
}
