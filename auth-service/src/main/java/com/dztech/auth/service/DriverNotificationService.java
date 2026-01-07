package com.dztech.auth.service;

import com.dztech.auth.dto.TripConfirmedNotificationRequest;
import com.dztech.auth.model.DriverProfile;
import com.dztech.auth.repository.DriverProfileRepository;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class DriverNotificationService {

    private static final Logger log = LoggerFactory.getLogger(DriverNotificationService.class);

    private final DriverProfileRepository driverProfileRepository;
    private final ObjectProvider<FirebaseMessaging> firebaseMessagingProvider;

    public DriverNotificationService(
            DriverProfileRepository driverProfileRepository, ObjectProvider<FirebaseMessaging> firebaseMessagingProvider) {
        this.driverProfileRepository = driverProfileRepository;
        this.firebaseMessagingProvider = firebaseMessagingProvider;
    }

    public void sendTripConfirmation(TripConfirmedNotificationRequest request) {
        log.info("Starting trip confirmation notification for bookingId: {}", request.bookingId());

        FirebaseMessaging messaging = firebaseMessagingProvider.getIfAvailable();
        if (messaging == null) {
            log.warn("FirebaseMessaging bean is unavailable; skipping trip confirmation push notification");
            return;
        }

        List<String> tokens = driverProfileRepository.findAllByFcmTokenIsNotNull().stream()
                .map(DriverProfile::getFcmToken)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .toList();

        log.debug("Found {} driver FCM tokens for notification", tokens.size());

        if (tokens.isEmpty()) {
            log.info("No driver FCM tokens found; skipping trip confirmation notification");
            return;
        }

        Notification notification = Notification.builder()
                .setTitle("New trip confirmed")
                .setBody("Pickup at " + request.pickupAddress())
                .build();

        MulticastMessage message = MulticastMessage.builder()
                .setNotification(notification)
                .putData("type", "New Incoming Ride")
                .putData("bookingId", String.valueOf(request.bookingId()))
                .putData("pickup", request.pickupAddress())
                .putData("drop", request.dropAddress())
                .putData("startTime", request.startTime().toString())
                .putData("endTime", request.endTime().toString())
                .putData("customerName", request.customerName())
                .putData("estimatedFare", request.estimatedFare().toString())
                .putData("vehicleBrand", request.vehicleBrand())
                .putData("vehicleModel", request.vehicleModel())
                .putData("vehicleNumber", request.vehicleNumber() != null ? request.vehicleNumber() : "")
                .addAllTokens(tokens)
                .build();

        try {
            BatchResponse response = messaging.sendEachForMulticast(message);
            log.info(
                    "Trip confirmation push sent to drivers. success={}, failure={}",
                    response.getSuccessCount(),
                    response.getFailureCount());
            if (response.getFailureCount() > 0) {
                log.debug(
                        "Trip confirmation push failures: {}",
                        response.getResponses().stream()
                                .filter(r -> !r.isSuccessful())
                                .map(r -> r.getException() != null ? r.getException().getMessage() : "Unknown error")
                                .collect(Collectors.toList()));
            }
        } catch (FirebaseMessagingException ex) {
            log.error("Failed to send trip confirmation notification to drivers", ex);
        }
    }
}
