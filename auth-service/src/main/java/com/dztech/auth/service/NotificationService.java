package com.dztech.auth.service;

import com.dztech.auth.dto.NotificationRequest;
import com.dztech.auth.dto.NotificationResponse;
import com.dztech.auth.model.Notification;
import com.dztech.auth.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Transactional(readOnly = true)
    public NotificationResponse getNotificationsByAppId(String appId) {
        validateAppId(appId);
        List<Notification> notifications = notificationRepository.findByAppIdOrderByTimeDesc(appId);

        List<NotificationResponse.NotificationData> notificationData = notifications.stream()
                .map(this::mapToNotificationData)
                .collect(Collectors.toList());

        return NotificationResponse.builder()
                .success(true)
                .data(notificationData)
                .build();
    }

    @Transactional
    public NotificationResponse createNotification(NotificationRequest request) {
        validateAppId(request.getAppId());

        Notification notification = Notification.builder()
                .title(request.getTitle())
                .message(request.getMessage())
                .time(request.getTime())
                .appId(request.getAppId())
                .build();

        Notification saved = notificationRepository.save(notification);

        // Return the newly created notification
        List<NotificationResponse.NotificationData> notificationData = List.of(mapToNotificationData(saved));

        return NotificationResponse.builder()
                .success(true)
                .data(notificationData)
                .build();
    }

    @Transactional
    public NotificationResponse updateNotification(Long id, NotificationRequest request) {
        validateAppId(request.getAppId());

        Notification existing = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found with id: " + id));

        Notification updated = Notification.builder()
                .id(existing.getId())
                .title(request.getTitle())
                .message(request.getMessage())
                .time(request.getTime())
                .appId(request.getAppId())
                .createdAt(existing.getCreatedAt())
                .build();

        Notification saved = notificationRepository.save(updated);

        // Return the updated notification
        List<NotificationResponse.NotificationData> notificationData = List.of(mapToNotificationData(saved));

        return NotificationResponse.builder()
                .success(true)
                .data(notificationData)
                .build();
    }

    @Transactional
    public void deleteNotification(Long id) {
        if (!notificationRepository.existsById(id)) {
            throw new IllegalArgumentException("Notification not found with id: " + id);
        }
        notificationRepository.deleteById(id);
    }

    private NotificationResponse.NotificationData mapToNotificationData(Notification notification) {
        return NotificationResponse.NotificationData.builder()
                .id(notification.getId().toString())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .time(TIME_FORMATTER.format(notification.getTime().atZone(java.time.ZoneOffset.UTC)))
                .isRead(false) // For now, always return false since we don't have user-specific read status
                .build();
    }

    private void validateAppId(String appId) {
        if (!"rydd".equals(appId) && !"rydc".equals(appId)) {
            throw new IllegalArgumentException("Invalid app ID. Must be 'rydd' or 'rydc'");
        }
    }
}
