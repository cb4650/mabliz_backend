package com.dztech.auth.service;

import com.dztech.auth.exception.ResourceNotFoundException;
import com.dztech.auth.model.DriverProfile;
import com.dztech.auth.repository.DriverProfileRepository;
import com.dztech.auth.storage.DocumentStorageService;
import java.util.Locale;
import java.util.function.Function;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class DriverDocumentAccessService {

    private final DriverProfileRepository driverProfileRepository;
    private final DocumentStorageService documentStorageService;

    public DriverDocumentAccessService(
            DriverProfileRepository driverProfileRepository, DocumentStorageService documentStorageService) {
        this.driverProfileRepository = driverProfileRepository;
        this.documentStorageService = documentStorageService;
    }

    public DriverDocumentResource getProfileDocument(
            Long requesterId, boolean requesterIsAdmin, Long driverId, String rawLabel) {
        DriverProfile profile = driverProfileRepository
                .findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found"));

        if (requesterId == null) {
            throw new AccessDeniedException("Authentication is required to access this document");
        }

        if (!requesterIsAdmin && !profile.getUserId().equals(requesterId)) {
            throw new AccessDeniedException("You are not allowed to access this document");
        }

        ProfileDocumentSlot slot = ProfileDocumentSlot.fromLabel(rawLabel);
        if (slot == null) {
            throw new ResourceNotFoundException("Document not found");
        }

        DocumentContent content = slot.resolve(profile, documentStorageService);
        if (content.data() == null || content.data().length == 0) {
            throw new ResourceNotFoundException("Document not found");
        }

        String contentType = StringUtils.hasText(content.contentType())
                ? content.contentType()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        boolean inlineImage = contentType.toLowerCase(Locale.ROOT).startsWith("image/");

        return new DriverDocumentResource(slot.label(), contentType, inlineImage, content.data());
    }

    private enum ProfileDocumentSlot {
        PROFILE_PHOTO(
                "profilePhoto",
                DriverProfile::getProfilePhotoObject,
                DriverProfile::getProfilePhoto,
                DriverProfile::getProfilePhotoContentType),
        LICENSE_FRONT(
                "licenseFront",
                DriverProfile::getLicenseFrontObject,
                DriverProfile::getLicenseFront,
                DriverProfile::getLicenseFrontContentType),
        LICENSE_BACK(
                "licenseBack",
                DriverProfile::getLicenseBackObject,
                DriverProfile::getLicenseBack,
                DriverProfile::getLicenseBackContentType),
        GOV_ID_FRONT(
                "govIdFront",
                DriverProfile::getGovIdFrontObject,
                DriverProfile::getGovIdFront,
                DriverProfile::getGovIdFrontContentType),
        GOV_ID_BACK(
                "govIdBack",
                DriverProfile::getGovIdBackObject,
                DriverProfile::getGovIdBack,
                DriverProfile::getGovIdBackContentType);

        private final String label;
        private final Function<DriverProfile, String> objectNameExtractor;
        private final Function<DriverProfile, byte[]> legacyDataExtractor;
        private final Function<DriverProfile, String> contentTypeExtractor;

        ProfileDocumentSlot(
                String label,
                Function<DriverProfile, String> objectNameExtractor,
                Function<DriverProfile, byte[]> legacyDataExtractor,
                Function<DriverProfile, String> contentTypeExtractor) {
            this.label = label;
            this.objectNameExtractor = objectNameExtractor;
            this.legacyDataExtractor = legacyDataExtractor;
            this.contentTypeExtractor = contentTypeExtractor;
        }

        static ProfileDocumentSlot fromLabel(String rawLabel) {
            if (!StringUtils.hasText(rawLabel)) {
                return null;
            }
            String trimmed = rawLabel.trim();
            for (ProfileDocumentSlot slot : values()) {
                if (slot.label.equalsIgnoreCase(trimmed)) {
                    return slot;
                }
            }
            return null;
        }

        DocumentContent resolve(DriverProfile profile, DocumentStorageService storageService) {
            String objectName = objectNameExtractor.apply(profile);
            byte[] data = null;
            if (StringUtils.hasText(objectName)) {
                data = storageService.download(objectName).orElse(null);
            }
            if ((data == null || data.length == 0)) {
                byte[] legacyData = legacyDataExtractor.apply(profile);
                if (legacyData != null && legacyData.length > 0) {
                    data = legacyData;
                }
            }
            return new DocumentContent(data, contentTypeExtractor.apply(profile));
        }

        String label() {
            return label;
        }
    }

    private record DocumentContent(byte[] data, String contentType) {}

    public record DriverDocumentResource(String label, String contentType, boolean inlineImage, byte[] data) {}
}
