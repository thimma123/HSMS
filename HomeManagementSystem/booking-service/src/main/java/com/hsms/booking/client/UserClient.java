package com.hsms.booking.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client for User Service
 */
@FeignClient(name = "user-service", contextId = "userClient")
public interface UserClient {

    @GetMapping("/api/customers/customerId/{customerId}")
    CustomerDTO getCustomerById(@PathVariable Long customerId);
}