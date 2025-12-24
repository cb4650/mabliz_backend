package com.dztech.rayder.service;

import com.dztech.rayder.dto.AddressDto;
import com.dztech.rayder.dto.SavedAddressRequest;
import com.dztech.rayder.dto.SavedAddressResponse;
import com.dztech.rayder.exception.ResourceNotFoundException;
import com.dztech.rayder.model.AddressDetails;
import com.dztech.rayder.model.SavedAddress;
import com.dztech.rayder.repository.SavedAddressRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SavedAddressService {

    private final SavedAddressRepository savedAddressRepository;

    public SavedAddressService(SavedAddressRepository savedAddressRepository) {
        this.savedAddressRepository = savedAddressRepository;
    }

    @Transactional
    public SavedAddressResponse createAddress(Long userId, SavedAddressRequest request) {
        String label = request.label().trim();
        validateLabelUniqueness(userId, label, null);

        SavedAddress entity = SavedAddress.builder()
                .userId(userId)
                .label(label)
                .receiverPhoneNumber(request.receiverPhoneNumber().trim())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .address(toAddressDetails(request.address()))
                .build();

        SavedAddress saved = savedAddressRepository.save(entity);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<SavedAddressResponse> getAddresses(Long userId) {
        return savedAddressRepository.findByUserIdOrderByIdAsc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public SavedAddressResponse updateAddress(Long userId, Long addressId, SavedAddressRequest request) {
        SavedAddress existing = savedAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Saved address not found for current user"));

        String label = request.label().trim();
        validateLabelUniqueness(userId, label, addressId);

        existing.setLabel(label);
        existing.setReceiverPhoneNumber(request.receiverPhoneNumber().trim());
        existing.setLatitude(request.latitude());
        existing.setLongitude(request.longitude());
        existing.setAddress(toAddressDetails(request.address()));

        SavedAddress updated = savedAddressRepository.save(existing);
        return toResponse(updated);
    }

    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        SavedAddress existing = savedAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Saved address not found for current user"));
        savedAddressRepository.delete(existing);
    }

    private void validateLabelUniqueness(Long userId, String label, Long excludedId) {
        boolean exists = excludedId == null
                ? savedAddressRepository.existsByUserIdAndLabel(userId, label)
                : savedAddressRepository.existsByUserIdAndLabelAndIdNot(userId, label, excludedId);
        if (exists) {
            throw new IllegalArgumentException("Label already exists for current user");
        }
    }

    private AddressDetails toAddressDetails(AddressDto address) {
        return new AddressDetails(
                address.houseName().trim(),
                address.addressLine1().trim(),
                address.addressLine2() != null ? address.addressLine2().trim() : null);
    }

    private AddressDto toAddressDto(AddressDetails address) {
        return new AddressDto(
                address.getHouseName(),
                address.getAddressLine1(),
                address.getAddressLine2());
    }

    private SavedAddressResponse toResponse(SavedAddress address) {
        return new SavedAddressResponse(
                address.getId(),
                address.getLabel(),
                address.getReceiverPhoneNumber(),
                address.getLatitude(),
                address.getLongitude(),
                toAddressDto(address.getAddress()));
    }
}
