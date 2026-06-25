package com.hsms.notificationservice.scheduler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.hsms.notificationservice.entity.Notification;
import com.hsms.notificationservice.repository.NotificationRepository;
import com.hsms.notificationservice.service.NotificationService;

@Component
public class NotificationRetryScheduler {

	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private NotificationService notificationService;

	// Run every 60 seconds
	@Scheduled(cron = "0 * * * * *")
	public void retryFailedNotifications() {
		List<Notification> failedNotifications = notificationRepository.findByStatus("FAILED");
		for (Notification notification : failedNotifications) {
			if (notification.getRetryCount() == null) {
				notification.setRetryCount(0);
			}
			if (notification.getRetryCount() < 3) {
				notification.setRetryCount(notification.getRetryCount() + 1);
				notification.setStatus("PENDING");
				notificationRepository.save(notification);
				notificationService.sendNotificationAsync(notification);
			} else {
				notification.setStatus("MAX_RETRIES_EXCEEDED");
				notificationRepository.save(notification);
			}
		}
	}
}
