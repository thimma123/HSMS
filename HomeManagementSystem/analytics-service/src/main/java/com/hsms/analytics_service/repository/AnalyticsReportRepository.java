package com.hsms.analytics_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.hsms.analytics_service.entity.AnalyticsReport;

public interface AnalyticsReportRepository extends JpaRepository<AnalyticsReport, Long> {
	
}