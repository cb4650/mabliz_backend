package com.dztech.rayder.controller;

import com.dztech.rayder.dto.SavedAddressDeleteResponse;
import com.dztech.rayder.dto.SavedAddressListResponse;
import com.dztech.rayder.dto.SavedAddressOperationResponse;
import com.dztech.rayder.dto.SavedAddressRequest;
import com.dztech.rayder.dto.SavedAddressResponse;
import com.dztech.rayder.security.AuthenticatedUserProvider;
import com.dztech.rayder.service.SavedAddressService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/saved-addresses")
public class SavedAddressController {

    private final SavedAddressService savedAddressService;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public SavedAddressController(
            SavedAddressService savedAddressService,
            AuthenticatedUserProvider authenticatedUserProvider) {
        this.savedAddressService = savedAddressService;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @PostMapping
    public ResponseEntity<SavedAddressOperationResponse> createAddress(
            @RequestBody @Valid SavedAddressRequest request) {
        Long userId = authenticatedUserProvider.getCurrentUserId();
        SavedAddressResponse address = savedAddressService.createAddress(userId, request);
        SavedAddressOperationResponse response =
                new SavedAddressOperationResponse(true, "Address saved successfully", address);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<SavedAddressListResponse> listAddresses() {
        Long userId = authenticatedUserProvider.getCurrentUserId();
        SavedAddressListResponse response =
                new SavedAddressListResponse(true, savedAddressService.getAddresses(userId));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SavedAddressOperationResponse> updateAddress(
            @PathVariable("id") Long id, @RequestBody @Valid SavedAddressRequest request) {
        Long userId = authenticatedUserProvider.getCurrentUserId();
        SavedAddressResponse address = savedAddressService.updateAddress(userId, id, request);
        SavedAddressOperationResponse response =
                new SavedAddressOperationResponse(true, "Address updated successfully", address);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SavedAddressDeleteResponse> deleteAddress(@PathVariable("id") Long id) {
        Long userId = authenticatedUserProvider.getCurrentUserId();
        savedAddressService.deleteAddress(userId, id);
        return ResponseEntity.ok(new SavedAddressDeleteResponse(true, "Address deleted successfully"));
    }
}
