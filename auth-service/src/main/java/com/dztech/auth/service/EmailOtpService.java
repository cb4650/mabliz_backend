package com.dztech.auth.service;

import com.dztech.auth.client.EmailOtpSender;
import com.dztech.auth.exception.EmailOtpException;
import com.dztech.auth.exception.OtpBlockedException;
import com.dztech.auth.model.EmailVerificationToken;
import com.dztech.auth.model.OtpFailureTracking;
import com.dztech.auth.repository.EmailVerificationTokenRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class EmailOtpService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final EmailOtpSender emailOtpSender;
    private final OtpFailureTrackingService otpFailureTrackingService;
    private final Duration expiry;
    private final int otpLength;

    public EmailOtpService(
            EmailVerificationTokenRepository tokenRepository,
            EmailOtpSender emailOtpSender,
            OtpFailureTrackingService otpFailureTrackingService,
            @Value("${email.otp.expiry-minutes:10}") long expiryMinutes,
            @Value("${email.otp.length:6}") int otpLength) {
        this.tokenRepository = tokenRepository;
        this.emailOtpSender = emailOtpSender;
        this.otpFailureTrackingService = otpFailureTrackingService;
        if (expiryMinutes <= 0) {
            throw new EmailOtpException("email.otp.expiry-minutes must be positive");
        }
        this.expiry = Duration.ofMinutes(expiryMinutes);
        if (otpLength < 4 || otpLength > 8) {
            throw new EmailOtpException("email.otp.length must be between 4 and 8");
        }
        this.otpLength = otpLength;
    }

    @Transactional
    public void sendVerificationOtp(Long userId, String email, String recipientName) {
        String normalizedEmail = normalizeEmail(email);
        tokenRepository.deleteByUserIdAndEmail(userId, normalizedEmail);

        String otpCode = generateOtp();
        Instant now = Instant.now();
        EmailVerificationToken token = EmailVerificationToken.builder()
                .userId(userId)
                .email(normalizedEmail)
                .otpCode(otpCode)
                .expiresAt(now.plus(expiry))
                .build();
        tokenRepository.save(token);

        emailOtpSender.sendOtp(normalizedEmail, recipientName, otpCode);
    }

    @Transactional
    public void verifyOtp(Long userId, String email, String otp) {
        String normalizedEmail = normalizeEmail(email);
        String normalizedOtp = normalizeOtp(otp);

        EmailVerificationToken token = tokenRepository
                .findFirstByUserIdAndEmailOrderByCreatedAtDesc(userId, normalizedEmail)
                .orElseThrow(() -> new EmailOtpException("No OTP request found for this email"));

        Instant now = Instant.now();
        if (token.isExpired(now)) {
            throw new EmailOtpException("OTP has expired");
        }

        if (!token.getOtpCode().equals(normalizedOtp)) {
            throw new EmailOtpException("Invalid OTP");
        }

        token.setVerifiedAt(now);
        tokenRepository.save(token);
        tokenRepository.deleteByUserIdAndEmail(userId, normalizedEmail);
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

    private String generateOtp() {
        int min = (int) Math.pow(10, otpLength - 1);
        int max = (int) Math.pow(10, otpLength) - 1;
        int value = ThreadLocalRandom.current().nextInt(min, max + 1);
        return String.format("%0" + otpLength + "d", value);
    }
}
