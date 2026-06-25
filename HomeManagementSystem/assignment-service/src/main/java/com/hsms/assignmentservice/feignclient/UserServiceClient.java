package com.hsms.assignmentservice.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.hsms.assignmentservice.model.UserDTO;

@FeignClient(name = "hsms-auth-service")
public interface UserServiceClient {

    @GetMapping("/api/auth/users/{id}")
    UserDTO getUserById(@PathVariable("id") Long id);
}
