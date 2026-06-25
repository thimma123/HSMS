package com.hsms.booking.event;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.hsms.booking.client.NotificationClient;
import com.hsms.booking.client.NotificationRequest;
import com.hsms.booking.dto.ServiceEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceEventListener {

	private final NotificationClient notificationClient;

	@Async
	@EventListener
	public void handleServiceEvent(ServiceEvent event) {
		log.info("Received service event: {} for request ID: {}", event.getEventType(), event.getRequestId());
		
		if ("REQUEST_CREATED".equals(event.getEventType())) {
			NotificationRequest notification = NotificationRequest.builder()
					.userId(event.getCustomerId())
					.title("Service Booking Created")
					.message(event.getMessage())
					.type("EMAIL")
					.build();
			try {
				notificationClient.sendNotification(notification);
				log.info("Sent creation notification for request: {}", event.getRequestId());
			} catch (Exception e) {
				log.error("Failed to send notification: {}", e.getMessage());
			}
		} else if ("REQUEST_CANCELLED".equals(event.getEventType())) {
			// Notify customer
			NotificationRequest customerNotification = NotificationRequest.builder()
					.userId(event.getCustomerId())
					.title("Service Booking Cancelled")
					.message("Your service request ID " + event.getRequestId() + " has been cancelled.")
					.type("EMAIL")
					.build();
			try {
				notificationClient.sendNotification(customerNotification);
				log.info("Sent cancellation notification to customer: {}", event.getCustomerId());
			} catch (Exception e) {
				log.error("Failed to send customer notification: {}", e.getMessage());
			}

			// Notify technician if assigned
			if (event.getTechnicianId() != null) {
				NotificationRequest techNotification = NotificationRequest.builder()
						.userId(event.getTechnicianId())
						.title("Service Cancelled")
						.message("Assigned service request " + event.getRequestId() + " has been cancelled.")
						.type("EMAIL")
						.build();
				try {
					notificationClient.sendNotification(techNotification);
					log.info("Sent cancellation notification to technician: {}", event.getTechnicianId());
				} catch (Exception e) {
					log.error("Failed to send technician notification: {}", e.getMessage());
				}
			}
		}
	}
}
