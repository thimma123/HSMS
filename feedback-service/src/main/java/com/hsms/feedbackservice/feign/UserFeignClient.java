package com.hsms.feedbackservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service")
public interface UserFeignClient {
    @PutMapping("/api/technicians/technicianId/{technicianId}/rating")
    void updateTechnicianRating(@PathVariable("technicianId") Long technicianId, @RequestParam("rating") Double rating);
}
