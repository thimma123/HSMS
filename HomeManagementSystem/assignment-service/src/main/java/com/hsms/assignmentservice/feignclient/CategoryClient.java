package com.hsms.assignmentservice.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.hsms.assignmentservice.model.CategoryResponseDTO;

@FeignClient(name = "catalog-service")
public interface CategoryClient {
    @GetMapping("/api/categories/{categoryId}")
    ResponseEntity<CategoryResponseDTO> getCategoryById(@PathVariable("categoryId") Long categoryId);
}
