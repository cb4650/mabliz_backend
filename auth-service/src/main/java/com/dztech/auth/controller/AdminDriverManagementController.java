package com.dztech.auth.controller;

import com.dztech.auth.dto.AdminDriverDetailResponse;
import com.dztech.auth.dto.AdminDriverListResponse;
import com.dztech.auth.dto.DriverFieldVerificationRequest;
import com.dztech.auth.dto.DriverFieldVerificationResponse;
import com.dztech.auth.dto.DriverFieldVerificationView;
import com.dztech.auth.security.AdminAuthenticatedUserProvider;
import com.dztech.auth.service.AdminDriverManagementService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/drivers")
public class AdminDriverManagementController {

    private final AdminDriverManagementService driverManagementService;
    private final AdminAuthenticatedUserProvider adminAuthenticatedUserProvider;

    public AdminDriverManagementController(
            AdminDriverManagementService driverManagementService,
            AdminAuthenticatedUserProvider adminAuthenticatedUserProvider) {
        this.driverManagementService = driverManagementService;
        this.adminAuthenticatedUserProvider = adminAuthenticatedUserProvider;
    }

    @GetMapping
    public ResponseEntity<AdminDriverListResponse> listDrivers() {
        return ResponseEntity.ok(driverManagementService.listDrivers());
    }

    @GetMapping("/{driverId}")
    public ResponseEntity<AdminDriverDetailResponse> getDriverDetail(@PathVariable Long driverId) {
        return ResponseEntity.ok(driverManagementService.getDriverDetail(driverId));
    }

    @PostMapping("/{driverId}/verify")
    public ResponseEntity<DriverFieldVerificationResponse> verifyField(
            @PathVariable Long driverId, @Valid @RequestBody DriverFieldVerificationRequest request) {
        Long adminId = adminAuthenticatedUserProvider.getCurrentAdminId();
        DriverFieldVerificationView view = driverManagementService.verifyField(adminId, driverId, request);
        return ResponseEntity.ok(new DriverFieldVerificationResponse(true, "Driver field updated", view));
    }
}
