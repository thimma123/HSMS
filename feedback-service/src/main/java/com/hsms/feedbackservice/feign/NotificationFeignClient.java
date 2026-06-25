package com.hsms.feedbackservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.hsms.feedbackservice.dto.NotificationDTO;

@FeignClient(name = "notification-service")
public interface NotificationFeignClient {
    @PostMapping("/api/notifications")
    void sendNotification(@RequestBody NotificationDTO notification);
}
