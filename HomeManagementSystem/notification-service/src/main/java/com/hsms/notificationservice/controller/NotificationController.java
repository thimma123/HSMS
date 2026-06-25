package com.hsms.notificationservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.hsms.notificationservice.dto.NotificationDTO;
import com.hsms.notificationservice.entity.Notification;
import com.hsms.notificationservice.service.NotificationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping
    public Notification saveNotification(
            @Valid @RequestBody NotificationDTO notificationDTO) {

        System.out.println("================================");
        System.out.println("Notification Received");
        System.out.println("User Id : " + notificationDTO.getUserId());
        System.out.println("Message : " + notificationDTO.getMessage());
        System.out.println("Status  : " + notificationDTO.getStatus());
        System.out.println("================================");

        Notification notification = new Notification();

        notification.setUserId(notificationDTO.getUserId());
        notification.setMessage(notificationDTO.getMessage());
        notification.setStatus(notificationDTO.getStatus());

        Notification saved = notificationService.saveNotification(notification);
        return saved;
    }

    @GetMapping
    public List<Notification> getAllNotifications() {
        return notificationService.getAllNotifications();
    }

    @GetMapping("/{id}")
    public Notification getNotificationById(@PathVariable Long id) {
        return notificationService.getNotificationById(id);
    }

    @PutMapping("/{id}")
    public Notification updateNotification(
            @PathVariable Long id,
            @Valid @RequestBody NotificationDTO notificationDTO) {

        Notification notification = new Notification();

        notification.setUserId(notificationDTO.getUserId());
        notification.setMessage(notificationDTO.getMessage());
        notification.setStatus(notificationDTO.getStatus());

        return notificationService.updateNotification(id, notification);
    }

    @DeleteMapping("/{id}")
    public String deleteNotification(@PathVariable Long id) {

        notificationService.deleteNotification(id);

        return "Notification deleted successfully";
    }
}