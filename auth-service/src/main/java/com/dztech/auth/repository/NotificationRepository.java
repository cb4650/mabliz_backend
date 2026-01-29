package com.dztech.auth.repository;

import com.dztech.auth.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByAppIdOrderByTimeDesc(String appId);

    List<Notification> findByAppId(String appId);
}
