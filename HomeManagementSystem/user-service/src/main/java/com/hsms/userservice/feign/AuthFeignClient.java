package com.hsms.userservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.hsms.userservice.model.UserProfileResponseDTO;


@FeignClient(name = "hsms-auth-service")
public interface AuthFeignClient {

    @GetMapping("/api/auth/users/{id}")
    UserProfileResponseDTO getUserById(@PathVariable("id") Long id);
}