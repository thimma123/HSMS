package com.hsms.notificationservice.service;

import java.util.List;

import com.hsms.notificationservice.entity.Notification;

public interface NotificationService {

    Notification saveNotification(Notification notification);

    List<Notification> getAllNotifications();

    Notification getNotificationById(Long id);

    Notification updateNotification(Long id, Notification notification);

    void deleteNotification(Long id);

    void sendNotificationAsync(Notification notification);
}