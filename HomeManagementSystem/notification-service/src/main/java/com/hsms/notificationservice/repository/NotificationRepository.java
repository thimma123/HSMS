package com.hsms.notificationservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hsms.notificationservice.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

	java.util.List<Notification> findByStatus(String status);
}