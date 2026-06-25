package com.hsms.feedbackservice.event;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.hsms.feedbackservice.dto.NotificationDTO;
import com.hsms.feedbackservice.feign.NotificationFeignClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeedbackEventListener {

	private final NotificationFeignClient notificationFeignClient;

	@Async
	@EventListener
	public void handleFeedbackSubmittedEvent(FeedbackSubmittedEvent event) {
		log.info("Received FeedbackSubmittedEvent for user ID: {}", event.getUserId());
		
		NotificationDTO notification = new NotificationDTO();
		notification.setUserId(event.getUserId());
		notification.setMessage(event.getMessage());
		notification.setStatus("SENT");

		try {
			notificationFeignClient.sendNotification(notification);
			log.info("Sent feedback success notification successfully");
		} catch (Exception e) {
			log.error("Failed to send feedback success notification: {}", e.getMessage());
		}
	}
}
