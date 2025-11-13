package com.dztech.auth.controller;

import com.dztech.auth.dto.AdminLoginResponse;
import com.dztech.auth.dto.AdminRegistrationRequest;
import com.dztech.auth.dto.AdminRegistrationResponse;
import com.dztech.auth.dto.OtpRequest;
import com.dztech.auth.dto.OtpRequestResponse;
import com.dztech.auth.dto.OtpVerificationRequest;
import com.dztech.auth.model.AppId;
import com.dztech.auth.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/register")
    public ResponseEntity<AdminRegistrationResponse> register(@RequestBody @Valid AdminRegistrationRequest request) {
        AdminRegistrationResponse response = adminService.registerAdmin(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/otp/request")
    public ResponseEntity<OtpRequestResponse> requestOtp(
            @RequestHeader("appId") String appIdHeader, @RequestBody @Valid OtpRequest request) {
        AppId appId = AppId.fromHeader(appIdHeader);
        OtpRequestResponse response = adminService.requestOtp(request, appId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AdminLoginResponse> login(
            @RequestHeader("appId") String appIdHeader, @RequestBody @Valid OtpVerificationRequest request) {
        AppId appId = AppId.fromHeader(appIdHeader);
        AdminLoginResponse response = adminService.verifyOtpAndLogin(request, appId);
        return ResponseEntity.ok(response);
    }
}
