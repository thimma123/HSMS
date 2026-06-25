package com.hsms.assignmentservice.event;

import java.time.LocalDateTime;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.hsms.assignmentservice.feignclient.NotificationClient;
import com.hsms.assignmentservice.model.NotificationDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AssignmentEventListener {

	private final NotificationClient notificationClient;

	@Async
	@EventListener
	public void handleAssignmentEvent(AssignmentEvent event) {
		log.info("Received assignment event: {} for user ID: {}", event.getEventType(), event.getUserId());
		NotificationDTO notification = new NotificationDTO(
				event.getUserId(),
				event.getMessage(),
				"SENT",
				LocalDateTime.now()
		);
		try {
			notificationClient.sendNotification(notification);
			log.info("Successfully sent Feign notification for assignment event");
		} catch (Exception e) {
			log.error("Failed to send notification via Feign client: {}", e.getMessage());
		}
	}
}
