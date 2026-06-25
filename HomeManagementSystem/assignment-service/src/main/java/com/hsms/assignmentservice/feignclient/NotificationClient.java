package com.hsms.assignmentservice.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.hsms.assignmentservice.model.NotificationDTO;

@FeignClient(name = "notification-service")
public interface NotificationClient {
	@PostMapping("/api/notifications")
    void sendNotification(@RequestBody NotificationDTO notification);
}