package com.hsms.notificationservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.hsms.notificationservice.dto.UserProfileDTO;

@FeignClient(name = "hsms-auth-service")
public interface AuthServiceClient {
    @GetMapping("/api/auth/users/{id}")
    ResponseEntity<UserProfileDTO> getUserById(@PathVariable("id") Long id);
}
