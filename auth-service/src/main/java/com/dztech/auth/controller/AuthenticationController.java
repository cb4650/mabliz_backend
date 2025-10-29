package com.dztech.auth.controller;

import com.dztech.auth.dto.LoginResponse;
import com.dztech.auth.dto.OtpRequest;
import com.dztech.auth.dto.OtpRequestResponse;
import com.dztech.auth.dto.OtpVerificationRequest;
import com.dztech.auth.dto.TokenRefreshRequest;
import com.dztech.auth.dto.TokenRefreshResponse;
import com.dztech.auth.model.AppId;
import com.dztech.auth.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestHeader("appId") String appIdHeader, @RequestBody @Valid OtpVerificationRequest request) {
        AppId appId = AppId.fromHeader(appIdHeader);
        LoginResponse response = authenticationService.verifyOtp(request, appId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/otp/request")
    public ResponseEntity<OtpRequestResponse> requestOtp(
            @RequestHeader("appId") String appIdHeader, @RequestBody @Valid OtpRequest request) {
        AppId appId = AppId.fromHeader(appIdHeader);
        OtpRequestResponse response = authenticationService.requestOtp(request, appId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refresh(@RequestBody @Valid TokenRefreshRequest request) {
        TokenRefreshResponse response = authenticationService.refreshToken(request);
        return ResponseEntity.ok(response);
    }
}
