package com.abc.paymentservice.event;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.abc.paymentservice.feignclient.NotificationClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

	private final NotificationClient notificationClient;

	@Async
	@EventListener
	public void handlePaymentSuccessEvent(PaymentSuccessEvent event) {
		log.info("Received PaymentSuccessEvent for request ID: {}", event.getServiceRequestId());
		Map<String, Object> notif = new HashMap<>();
		notif.put("userId", event.getCustomerId());
		notif.put("message", "Payment of " + event.getAmount() + " succeeded for service request ID: " + event.getServiceRequestId());
		notif.put("status", "SENT");

		try {
			notificationClient.sendNotification(notif);
			log.info("Sent payment success notification successfully");
		} catch (Exception e) {
			log.error("Failed to send payment success notification: {}", e.getMessage());
		}
	}
}
