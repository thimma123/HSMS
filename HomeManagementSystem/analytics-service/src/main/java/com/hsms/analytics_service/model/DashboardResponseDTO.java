package com.hsms.analytics_service.model;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponseDTO {
	private Long reportId;
    private Integer totalBookings;
    private double revenue;
    private List<TechnicianDetailResponseDTO> topTechnicians;          
    private List<CategoryDistributionDTO> categoryDistribution;
    private Integer completedServices;
    private Integer cancelledServices;
    private double paymentSuccessRate;
    private double paymentFailureRate;
    private double averageRating;
    private double serviceCompletionRate;
    private Integer pendingServices;
    private Integer assignedServices;
    private java.util.Map<String, Double> revenueByCategory;
    private java.util.Map<String, Long> technicianProductivity;
    private LocalDateTime generatedAt;
}
