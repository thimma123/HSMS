package com.hsms.analytics_service.service;

import com.hsms.analytics_service.model.DashboardResponseDTO;

public interface AnalyticsService {
    DashboardResponseDTO getDashboard(String startDate, String endDate, Long categoryId, String city, Long technicianId, String status);
}