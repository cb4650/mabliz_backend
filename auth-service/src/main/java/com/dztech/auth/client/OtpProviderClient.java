package com.dztech.auth.client;

import com.dztech.auth.exception.OtpProviderException;
import com.dztech.auth.model.AppId;
import java.net.URI;
import java.time.Duration;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.google.gson.Gson;

@Component
public class OtpProviderClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String apiKey;
    private final String templateBasePath;
    private final String customerSmsTemplate;
    private final String driverSmsTemplate;
    Gson gson = new Gson();

    public OtpProviderClient(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${otp.provider.base-url}") String baseUrl,
            @Value("${otp.provider.api-key}") String apiKey,
            @Value("${otp.provider.sms-template.base-path:AUTOGEN2}") String templateBasePath,
            @Value("${otp.provider.customer.sms-template:OTP_Customer}") String customerSmsTemplate,
            @Value("${otp.provider.driver.sms-template:OTP_partner}") String driverSmsTemplate) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
        this.baseUrl = requireNonBlank(baseUrl, "otp.provider.base-url must be configured");
        this.apiKey = requireNonBlank(apiKey, "otp.provider.api-key must be configured");
        this.templateBasePath = requireNonBlank(
                templateBasePath, "otp.provider.sms-template.base-path must be configured");
        this.customerSmsTemplate = requireNonBlank(
                customerSmsTemplate, "otp.provider.customer.sms-template must be configured");
        this.driverSmsTemplate = requireNonBlank(
                driverSmsTemplate, "otp.provider.driver.sms-template must be configured");
    }

    public String sendOtp(String phone, AppId appId) {
        String normalizedPhone = normalizePhone(phone);
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .pathSegment(apiKey)
                .pathSegment("SMS")
                .pathSegment(normalizedPhone)
                .pathSegment(templateBasePath)
                .pathSegment(resolveTemplate(appId))
                .build()
                .encode()
                .toUri();
        OtpProviderResponse response = execute(uri);
        ensureSuccess(response, "Failed to request OTP: ");
        return response.details();
    }

    public void verifyOtp(String phone, String otp) {
        String normalizedPhone = normalizePhone(phone);
        String normalizedOtp = normalizeOtp(otp);
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .pathSegment(apiKey)
                .pathSegment("SMS")
                .pathSegment("VERIFY3")
                .pathSegment(normalizedPhone)
                .pathSegment(normalizedOtp)
                .build()
                .encode()
                .toUri();
        OtpProviderResponse response = execute(uri);
        System.out.println(gson.toJson(response));
        ensureSuccess(response, "Failed to verify OTP: ");
    }

    private OtpProviderResponse execute(URI uri) {
        try {
            ResponseEntity<OtpProviderResponse> entity = restTemplate.getForEntity(uri, OtpProviderResponse.class);
            OtpProviderResponse body = entity.getBody();
            if (body == null) {
                throw new OtpProviderException("No response from OTP provider");
            }
            return body;
        } catch (RestClientException ex) {
            throw new OtpProviderException("OTP provider call failed", ex);
        }
    }

    private void ensureSuccess(OtpProviderResponse response, String errorPrefix) {
        if (!"success".equalsIgnoreCase(response.status())) {
            String details = StringUtils.hasText(response.details()) ? response.details() : "Unknown error";
            throw new OtpProviderException(errorPrefix + details);
        }
    }

    private String resolveTemplate(AppId appId) {
        if (appId == null) {
            return customerSmsTemplate;
        }
        return switch (appId) {
            case ALL, RYDC -> customerSmsTemplate;
            case RYDD -> driverSmsTemplate;
        };
    }

    private String normalizePhone(String phone) {
        if (phone == null) {
            throw new OtpProviderException("Phone number is required for OTP operations");
        }
        String digitsOnly = phone.chars()
                .filter(Character::isDigit)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        if (!StringUtils.hasText(digitsOnly)) {
            throw new OtpProviderException("Phone number must contain digits");
        }
        return digitsOnly;
    }

    private String normalizeOtp(String otp) {
        if (otp == null) {
            throw new OtpProviderException("OTP is required for verification");
        }
        String trimmed = otp.trim();
        if (!trimmed.matches("\\d{4,8}")) {
            throw new OtpProviderException("OTP must be numeric");
        }
        return trimmed;
    }

    private String requireNonBlank(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new OtpProviderException(message);
        }
        return value.trim();
    }

    private record OtpProviderResponse(String Status, String Details) {
        String status() {
            return Objects.toString(Status, "");
        }

        String details() {
            return Objects.toString(Details, "");
        }
    }
}
