package com.dztech.auth.client;

import com.dztech.auth.exception.EmailOtpException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class EmailOtpSender {

    private static final Logger log = LoggerFactory.getLogger(EmailOtpSender.class);

    private final RestClient restClient;
    private final String consumerKey;
    private final String consumerSecret;
    private final boolean enabled;
    private final String from;
    private final String subject;
    private final String cc;
    private final String bcc;
    private final String textTemplate;
    private final String htmlTemplate;

    public EmailOtpSender(
            RestClient.Builder restClientBuilder,
            @Value("${email.otp.base-url}") String baseUrl,
            @Value("${email.otp.consumer-key:}") String consumerKey,
            @Value("${email.otp.consumer-secret:}") String consumerSecret,
            @Value("${email.otp.from}") String from,
            @Value("${email.otp.subject}") String subject,
            @Value("${email.otp.cc:}") String cc,
            @Value("${email.otp.bcc:}") String bcc,
            @Value("${email.otp.text-template}") String textTemplate,
            @Value("${email.otp.html-template}") String htmlTemplate) {

        this.restClient = restClientBuilder.baseUrl(requireNonBlank(baseUrl, "email.otp.base-url must be configured"))
                .build();
        this.consumerKey = trimToNull(consumerKey);
        this.consumerSecret = trimToNull(consumerSecret);
        this.enabled = StringUtils.hasText(this.consumerKey) && StringUtils.hasText(this.consumerSecret);
        if (!this.enabled) {
            log.warn("Email OTP consumer credentials are not configured; email verification emails will fail until set");
        }
        this.from = requireNonBlank(from, "email.otp.from must be configured");
        this.subject = requireNonBlank(subject, "email.otp.subject must be configured");
        this.cc = cc;
        this.bcc = bcc;
        this.textTemplate = requireNonBlank(textTemplate, "email.otp.text-template must be configured");
        this.htmlTemplate = requireNonBlank(htmlTemplate, "email.otp.html-template must be configured");
    }

    public void sendOtp(String email, String recipientName, String otp) {
        ensureReady();
        String resolvedText = renderTemplate(textTemplate, otp, recipientName);
        String resolvedHtml = renderTemplate(htmlTemplate, otp, recipientName);

        Map<String, Object> payload = new HashMap<>();
        payload.put("from", from);
        payload.put("to", email);
        payload.put("subject", subject);
        if (StringUtils.hasText(cc)) {
            payload.put("cc", cc);
        }
        if (StringUtils.hasText(bcc)) {
            payload.put("bcc", bcc);
        }
        payload.put("content", resolvedText);
        payload.put("html_content", resolvedHtml);

        try {
            restClient
                    .post()
                    .header("accept", "application/json")
                    .header("consumerKey", consumerKey)
                    .header("consumerSecret", consumerSecret)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            throw new EmailOtpException("Failed to send email verification OTP", ex);
        }
    }

    private String renderTemplate(String template, String otp, String recipientName) {
        String resolved = template.replace("{{OTP}}", otp);
        if (StringUtils.hasText(recipientName)) {
            resolved = resolved.replace("{{NAME}}", recipientName);
        }
        return resolved;
    }

    private String requireNonBlank(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new EmailOtpException(message);
        }
        return value.trim();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void ensureReady() {
        if (!enabled) {
            throw new EmailOtpException("Email OTP credentials are not configured");
        }
    }
}
