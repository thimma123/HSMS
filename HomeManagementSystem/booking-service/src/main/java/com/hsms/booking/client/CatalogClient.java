package com.hsms.booking.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client for Catalog Service
 */
@FeignClient(name = "catalog-service")
public interface CatalogClient {

	@GetMapping("/api/categories/{id}")
	CategoryDTO getCategoryById(@PathVariable("id") Long id);

	@GetMapping("/api/categories/{id}/exists")
	Boolean categoryExists(@PathVariable("id") Long id);
}
