package com.hsms.notificationservice.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.http.ResponseEntity;

import com.hsms.notificationservice.entity.Notification;
import com.hsms.notificationservice.repository.NotificationRepository;
import com.hsms.notificationservice.client.AuthServiceClient;
import com.hsms.notificationservice.dto.UserProfileDTO;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private AuthServiceClient authServiceClient;

    @Override
    public Notification saveNotification(Notification notification) {
        notification.setStatus("PENDING");
        Notification saved = notificationRepository.save(notification);
        sendNotificationAsync(saved);
        return saved;
    }

    @Override
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    @Override
    public Notification getNotificationById(Long id) {
        return notificationRepository.findById(id).orElse(null);
    }

    @Override
    public Notification updateNotification(Long id, Notification notification) {

        Notification existingNotification =
                notificationRepository.findById(id).orElse(null);

        if (existingNotification != null) {

            existingNotification.setUserId(notification.getUserId());
            existingNotification.setMessage(notification.getMessage());
            existingNotification.setStatus(notification.getStatus());

            return notificationRepository.save(existingNotification);
        }

        return null;
    }

    @Override
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }

    @Override
    @Async
    public void sendNotificationAsync(Notification notification) {
        String email = null;
        try {
            if (notification.getEventType() == null) {
                notification.setEventType("SMS_OR_EMAIL");
            }
            if (notification.getChannel() == null) {
                notification.setChannel("EMAIL");
            }

            // Retrieve recipient email from auth-service
            if (notification.getUserId() != null) {
                try {
                    ResponseEntity<UserProfileDTO> response = authServiceClient.getUserById(notification.getUserId());
                    if (response != null && response.getBody() != null) {
                        email = response.getBody().getEmail();
                    }
                } catch (Exception e) {
                    System.err.println("Failed to fetch user email from auth-service: " + e.getMessage());
                }
            }

            if (email == null || email.trim().isEmpty()) {
                System.err.println("No email found for userId: " + notification.getUserId() + ". Defaulting to dummy-email@example.com for simulation.");
                email = "dummy-email@example.com";
            }
            
            notification.setRecipient(email);

            // Prepare simple mail message
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(email);
            mailMessage.setSubject("Home Management System Notification: " + notification.getEventType());
            mailMessage.setText(notification.getMessage());
            mailMessage.setFrom("sairohithroyal5@gmail.com");

            // Send mail
            mailSender.send(mailMessage);

            // Update status to SENT
            notification.setStatus("SENT");
            notification.setFailureReason(null);
            notificationRepository.save(notification);

            System.out.println("Notification sent successfully to: " + email);
        } catch (Exception e) {
            System.err.println("Error sending asynchronous notification: " + e.getMessage());
            // Update status to FAILED in case of SMTP errors
            notification.setStatus("FAILED");
            notification.setFailureReason(e.getMessage());
            if (email != null) {
                notification.setRecipient(email);
            }
            notificationRepository.save(notification);
        }
    }
}