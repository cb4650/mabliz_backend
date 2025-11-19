package com.dztech.auth.service;

import com.dztech.auth.client.OtpProviderClient;
import com.dztech.auth.dto.AdminLoginResponse;
import com.dztech.auth.dto.AdminRegistrationRequest;
import com.dztech.auth.dto.AdminRegistrationResponse;
import com.dztech.auth.dto.OtpRequest;
import com.dztech.auth.dto.OtpRequestResponse;
import com.dztech.auth.dto.OtpVerificationRequest;
import com.dztech.auth.model.Admin;
import com.dztech.auth.model.AppId;
import com.dztech.auth.repository.AdminRepository;
import com.dztech.auth.security.JwtTokenService;
import com.dztech.auth.security.JwtTokenService.JwtTokenPayload;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final JwtTokenService jwtTokenService;
    private final OtpProviderClient otpProviderClient;

    public AdminService(
            AdminRepository adminRepository,
            JwtTokenService jwtTokenService,
            OtpProviderClient otpProviderClient) {
        this.adminRepository = adminRepository;
        this.jwtTokenService = jwtTokenService;
        this.otpProviderClient = otpProviderClient;
    }

    @Transactional(readOnly = true)
    public OtpRequestResponse requestOtp(OtpRequest request, AppId appId) {
        String normalizedPhone = normalizePhone(request.phone());

        // Check if admin exists with this phone number
        boolean newUser = !adminRepository.existsByPhone(normalizedPhone);

        otpProviderClient.sendOtp(normalizedPhone, appId);
        return new OtpRequestResponse(true, "OTP sent successfully", newUser);
    }

    @Transactional
    public AdminLoginResponse verifyOtpAndLogin(OtpVerificationRequest request, AppId appId) {
        String normalizedPhone = normalizePhone(request.phone());
        String normalizedOtp = normalizeOtp(request.otp());

        // Verify admin exists
        Admin admin = adminRepository.findByPhone(normalizedPhone)
                .orElseThrow(() -> new IllegalArgumentException("Admin account not found for this phone number"));

        // Verify OTP
        otpProviderClient.verifyOtp(normalizedPhone, normalizedOtp);

        // Generate tokens
        TokenPair tokens = issueTokens(admin, appId);

        return new AdminLoginResponse(
                true,
                tokens.accessToken(),
                tokens.refreshToken(),
                admin.getId(),
                admin.getName(),
                admin.getPhone());
    }

    @Transactional
    public AdminRegistrationResponse registerAdmin(AdminRegistrationRequest request) {
        String normalizedPhone = normalizePhone(request.phone());

        // Check if admin already exists
        if (adminRepository.existsByPhone(normalizedPhone)) {
            throw new IllegalArgumentException("Admin already exists with this phone number");
        }

        Admin admin = Admin.builder()
                .name(request.name())
                .phone(normalizedPhone)
                .build();

        Admin savedAdmin = adminRepository.save(admin);

        return new AdminRegistrationResponse(
                true,
                "Admin registered successfully",
                savedAdmin.getId(),
                savedAdmin.getName(),
                savedAdmin.getPhone());
    }

    private TokenPair issueTokens(Admin admin, AppId appId) {
        Map<String, Object> additionalClaims = new HashMap<>();
        if (appId != null) {
            additionalClaims.put("appId", appId.value());
        }
        additionalClaims.put("role", "ADMIN");

        JwtTokenPayload payload = new JwtTokenPayload(
                admin.getId(),
                "admin_" + admin.getId(),
                "admin@local",
                admin.getPhone(),
                admin.getName(),
                additionalClaims);

        String accessToken = jwtTokenService.generateAccessToken(payload);
        String refreshToken = jwtTokenService.generateRefreshToken(payload);
        return new TokenPair(accessToken, refreshToken);
    }

    private String normalizePhone(String rawPhone) {
        String trimmed = rawPhone == null ? "" : rawPhone.trim();
        if (!StringUtils.hasText(trimmed)) {
            throw new IllegalArgumentException("Phone number is required");
        }
        String digitsOnly = trimmed.replaceAll("\\D", "");
        if (!StringUtils.hasText(digitsOnly)) {
            throw new IllegalArgumentException("Phone number must contain digits");
        }
        return digitsOnly;
    }

    private String normalizeOtp(String rawOtp) {
        String trimmed = rawOtp == null ? "" : rawOtp.trim();
        if (!StringUtils.hasText(trimmed)) {
            throw new IllegalArgumentException("OTP is required");
        }
        return trimmed;
    }

    private record TokenPair(String accessToken, String refreshToken) {}
}
