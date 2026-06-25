package com.hsms.execution_service.event;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.hsms.execution_service.feignclient.NotificationClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceRecordEventListener {

	private final NotificationClient notificationClient;

	@Async
	@EventListener
	public void handleServiceStartedEvent(ServiceStartedEvent event) {
		log.info("Received ServiceStartedEvent for request ID: {}", event.getServiceRequestId());
		Map<String, Object> notif = new HashMap<>();
		notif.put("userId", event.getCustomerId());
		notif.put("message", "Your service request ID " + event.getServiceRequestId() + " has been started by the technician.");
		notif.put("status", "SENT");

		try {
			notificationClient.sendNotification(notif);
			log.info("Sent start notification successfully");
		} catch (Exception e) {
			log.error("Failed to send start notification: {}", e.getMessage());
		}
	}

	@Async
	@EventListener
	public void handleServiceCompletedEvent(ServiceCompletedEvent event) {
		log.info("Received ServiceCompletedEvent for request ID: {}", event.getServiceRequestId());
		Map<String, Object> notif = new HashMap<>();
		notif.put("userId", event.getCustomerId());
		notif.put("message", "Service request ID " + event.getServiceRequestId() + " has been completed. Cost: " + event.getActualCost());
		notif.put("status", "SENT");

		try {
			notificationClient.sendNotification(notif);
			log.info("Sent complete notification successfully");
		} catch (Exception e) {
			log.error("Failed to send complete notification: {}", e.getMessage());
		}
	}
}
