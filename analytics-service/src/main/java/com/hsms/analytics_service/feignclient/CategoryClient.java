package com.hsms.analytics_service.feignclient;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import com.hsms.analytics_service.model.CategoryDistributionDTO;

@FeignClient(name = "catalog-service")
public interface CategoryClient {
	
    @GetMapping("/api/categories/distribution")
    List<CategoryDistributionDTO> getCategoryDistribution();
}