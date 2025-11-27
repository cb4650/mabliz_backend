package com.dztech.auth.service;

import com.dztech.auth.client.EmailOtpSender;
import com.dztech.auth.exception.EmailOtpException;
import com.dztech.auth.exception.OtpBlockedException;
import com.dztech.auth.model.DriverEmailVerificationToken;
import com.dztech.auth.repository.DriverEmailVerificationTokenRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class DriverEmailOtpService {

    private final DriverEmailVerificationTokenRepository tokenRepository;
    private final EmailOtpSender emailOtpSender;
    private final Duration expiry;
    private final int otpLength;

    public DriverEmailOtpService(
            DriverEmailVerificationTokenRepository tokenRepository,
            EmailOtpSender emailOtpSender,
            @Value("${driver.email.otp.expiry-seconds:300}") long expirySeconds,
            @Value("${driver.email.otp.length:6}") int otpLength) {
        this.tokenRepository = tokenRepository;
        this.emailOtpSender = emailOtpSender;
        if (expirySeconds <= 0) {
            throw new EmailOtpException("driver.email.otp.expiry-seconds must be positive");
        }
        this.expiry = Duration.ofSeconds(expirySeconds);
        if (otpLength < 4 || otpLength > 8) {
            throw new EmailOtpException("driver.email.otp.length must be between 4 and 8");
        }
        this.otpLength = otpLength;
    }

    public long getExpirySeconds() {
        return expiry.getSeconds();
    }

    @Transactional
    public void sendOtp(Long userId, String email, String name) {
        if (userId == null) {
            throw new EmailOtpException("User id is required to send driver email OTP");
        }
        String normalizedEmail = normalizeEmail(email);
        String normalizedName = normalizeName(name);

        DriverEmailVerificationToken existingForEmail = tokenRepository
                .findByEmail(normalizedEmail)
                .map(existing -> {
                    if (existing.getUserId() != null && !userId.equals(existing.getUserId())) {
                        throw new EmailOtpException("Email already in use by another driver");
                    }
                    return existing;
                })
                .orElse(null);

        DriverEmailVerificationToken token = existingForEmail;
        if (token == null) {
            token = tokenRepository.findByUserId(userId).orElse(null);
        }

        String otpCode = generateOtp();
        Instant now = Instant.now();

        if (token == null) {
            token = DriverEmailVerificationToken.builder()
                    .userId(userId)
                    .email(normalizedEmail)
                    .name(normalizedName)
                    .otpCode(otpCode)
                    .expiresAt(now.plus(expiry))
                    .build();
            token.stampCreation(now);
        } else {
            token.setUserId(userId);
            token.setEmail(normalizedEmail);
            token.setName(normalizedName);
            token.setOtpCode(otpCode);
            token.setExpiresAt(now.plus(expiry));
            token.setVerifiedAt(null);
            token.touch(now);
        }

        tokenRepository.save(token);

        emailOtpSender.sendOtp(normalizedEmail, normalizedName, otpCode);
    }

    @Transactional
    public DriverEmailVerificationToken verifyOtp(Long userId, String email, String otp) {
        if (userId == null) {
            throw new EmailOtpException("User id is required to verify driver email OTP");
        }
        String normalizedEmail = normalizeEmail(email);
        String normalizedOtp = normalizeOtp(otp);

        DriverEmailVerificationToken token = tokenRepository
                .findByEmail(normalizedEmail)
                .orElseThrow(() -> new EmailOtpException("No OTP request found for this email"));

        if (token.getUserId() != null && !userId.equals(token.getUserId())) {
            throw new EmailOtpException("OTP request does not belong to the current driver");
        }

        Instant now = Instant.now();
        if (token.isExpired(now)) {
            throw new EmailOtpException("OTP has expired");
        }

        if (!token.getOtpCode().equals(normalizedOtp)) {
            throw new EmailOtpException("Invalid OTP");
        }

        token.markVerified(now);
        tokenRepository.deleteByUserId(userId);
        return token;
    }

    private String normalizeEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new EmailOtpException("Email is required");
        }
        return email.trim().toLowerCase();
    }

    private String normalizeOtp(String otp) {
        if (!StringUtils.hasText(otp)) {
            throw new EmailOtpException("OTP is required");
        }
        String trimmed = otp.trim();
        if (!trimmed.matches("\\d{4,8}")) {
            throw new EmailOtpException("OTP must be numeric and between 4 to 8 digits");
        }
        return trimmed;
    }

    private String normalizeName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new EmailOtpException("Name is required");
        }
        return name.trim();
    }

    private String generateOtp() {
        int min = (int) Math.pow(10, otpLength - 1);
        int max = (int) Math.pow(10, otpLength) - 1;
        int value = ThreadLocalRandom.current().nextInt(min, max + 1);
        return String.format("%0" + otpLength + "d", value);
    }
}
