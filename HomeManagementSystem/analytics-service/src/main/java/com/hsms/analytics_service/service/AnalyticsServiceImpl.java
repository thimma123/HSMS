package com.hsms.analytics_service.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.hsms.analytics_service.entity.AnalyticsReport;
import com.hsms.analytics_service.feignclient.PaymentClient;
import com.hsms.analytics_service.feignclient.TechnicianClient;
import com.hsms.analytics_service.feignclient.BookingServiceClient;
import com.hsms.analytics_service.model.*;
import com.hsms.analytics_service.repository.AnalyticsReportRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsServiceImpl.class);

    private final AnalyticsReportRepository repo;
    private final BookingServiceClient bookingClient;
    private final TechnicianClient technicianClient;
    private final PaymentClient paymentClient;

    @Override
    public DashboardResponseDTO getDashboard(String startDate, String endDate,
                                             Long categoryId, String city,
                                             Long technicianId, String status) {

        List<ServiceRequestDetailResponseDTO> requests = bookingClient.getAllRequests();
        List<TechnicianDetailResponseDTO> technicians = technicianClient.getAllTechnicians();
        List<PaymentResponseDTO> payments = paymentClient.getAllPayments();

        LocalDateTime start = parseDate(startDate, true);
        LocalDateTime end = parseDate(endDate, false);

        List<ServiceRequestDetailResponseDTO> filteredRequests =
                filterRequests(requests, start, end, categoryId, city, technicianId, status);

        List<PaymentResponseDTO> filteredPayments = filterPayments(payments, filteredRequests);

        int totalBookings = filteredRequests.size();
        double revenue = calculateRevenue(filteredPayments);

        List<TechnicianDetailResponseDTO> topTechnicians = getTopTechnicians(technicians);
        List<CategoryDistributionDTO> categoryDistribution = getCategoryDistribution(filteredRequests);

        AnalyticsReport report = buildReport(totalBookings, revenue, filteredRequests,
                                             filteredPayments, technicians);
        repo.save(report);

        return buildResponse(report, topTechnicians, categoryDistribution,
                             filteredPayments, technicians, filteredRequests);
    }

    // ---------------- Helper Methods ----------------

    private LocalDateTime parseDate(String date, boolean isStart) {
        if (date == null || date.trim().isEmpty()) return null;
        try {
            if (date.contains("T")) {
                return LocalDateTime.parse(date);
            }
            return isStart ? LocalDate.parse(date).atStartOfDay()
                           : LocalDate.parse(date).atTime(23, 59, 59);
        } catch (Exception e) {
            log.warn("Failed to parse date {}: {}", date, e.getMessage());
            return null;
        }
    }

    private List<ServiceRequestDetailResponseDTO> filterRequests(List<ServiceRequestDetailResponseDTO> requests,
                                                                 LocalDateTime start, LocalDateTime end,
                                                                 Long categoryId, String city,
                                                                 Long technicianId, String status) {
        return requests.stream()
                .filter(r ->
                        (start == null || r.getScheduledDateTime() == null || !r.getScheduledDateTime().isBefore(start)) &&
                        (end == null || r.getScheduledDateTime() == null || !r.getScheduledDateTime().isAfter(end)) &&
                        (categoryId == null || categoryId.equals(r.getCategoryId())) &&
                        (city == null || city.trim().isEmpty() || city.equalsIgnoreCase(r.getCity())) &&
                        (technicianId == null || technicianId.equals(r.getTechnicianId())) &&
                        (status == null || status.trim().isEmpty() || status.equalsIgnoreCase(r.getStatus()))
                )
                .toList();
    }

    private List<PaymentResponseDTO> filterPayments(List<PaymentResponseDTO> payments,
                                                    List<ServiceRequestDetailResponseDTO> filteredRequests) {
        Set<Long> filteredRequestIds = filteredRequests.stream()
                .map(ServiceRequestDetailResponseDTO::getRequestId)
                .collect(Collectors.toSet());

        return payments.stream()
                .filter(p -> filteredRequestIds.contains(p.getServiceRequestId()))
                .toList();
    }

    private double calculateRevenue(List<PaymentResponseDTO> payments) {
        return payments.stream()
                .filter(p -> "SUCCESS".equalsIgnoreCase(p.getPaymentStatus()))
                .mapToDouble(PaymentResponseDTO::getAmount)
                .sum();
    }

    private List<TechnicianDetailResponseDTO> getTopTechnicians(List<TechnicianDetailResponseDTO> technicians) {
        return technicians.stream()
                .filter(t -> t.getRating() != null)
                .sorted(Comparator.comparing(TechnicianDetailResponseDTO::getRating).reversed())
                .limit(5)
                .toList();
    }

    private List<CategoryDistributionDTO> getCategoryDistribution(List<ServiceRequestDetailResponseDTO> requests) {
        return requests.stream()
                .collect(Collectors.groupingBy(r -> Optional.ofNullable(r.getCategoryId()).orElse(-1L),
                                               Collectors.counting()))
                .entrySet().stream()
                .map(entry -> new CategoryDistributionDTO(entry.getKey(), entry.getValue().intValue()))
                .toList();
    }

    private AnalyticsReport buildReport(int totalBookings, double revenue,
                                        List<ServiceRequestDetailResponseDTO> requests,
                                        List<PaymentResponseDTO> payments,
                                        List<TechnicianDetailResponseDTO> technicians) {

        long completedCount = requests.stream().filter(r -> List.of("COMPLETED", "PAID").contains(r.getStatus())).count();
        long cancelledCount = requests.stream().filter(r -> "CANCELLED".equalsIgnoreCase(r.getStatus())).count();

        long successPayments = payments.stream().filter(p -> "SUCCESS".equalsIgnoreCase(p.getPaymentStatus())).count();
        long totalPayments = payments.size();

        double successRate = totalPayments > 0 ? (double) successPayments / totalPayments * 100.0 : 0.0;
        double avgRating = technicians.stream().filter(t -> t.getRating() != null)
                                      .mapToDouble(TechnicianDetailResponseDTO::getRating)
                                      .average().orElse(0.0);
        double completionRate = totalBookings > 0 ? (double) completedCount / totalBookings * 100.0 : 0.0;

        AnalyticsReport report = new AnalyticsReport();
        report.setTotalBookings(totalBookings);
        report.setRevenue(revenue);
        report.setCompletedServices((int) completedCount);
        report.setCancelledServices((int) cancelledCount);
        report.setPaymentSuccessRate(successRate);
        report.setAverageRating(avgRating);
        report.setServiceCompletionRate(completionRate);
        report.setGeneratedAt(LocalDateTime.now());

        return report;
    }

    private DashboardResponseDTO buildResponse(AnalyticsReport report,
                                               List<TechnicianDetailResponseDTO> topTechnicians,
                                               List<CategoryDistributionDTO> categoryDistribution,
                                               List<PaymentResponseDTO> payments,
                                               List<TechnicianDetailResponseDTO> technicians,
                                               List<ServiceRequestDetailResponseDTO> requests) {

        Map<Long, String> reqToCatName = requests.stream()
                .collect(Collectors.toMap(ServiceRequestDetailResponseDTO::getRequestId,
                        r -> Optional.ofNullable(r.getCategoryName()).orElse("Unknown"),
                        (a, b) -> a));

        Map<String, Double> revenueByCategory = payments.stream()
                .filter(p -> "SUCCESS".equalsIgnoreCase(p.getPaymentStatus()))
                .collect(Collectors.groupingBy(
                        p -> reqToCatName.getOrDefault(p.getServiceRequestId(), "Unknown"),
                        Collectors.summingDouble(PaymentResponseDTO::getAmount)));

        Map<Long, String> techIdToName = technicians.stream()
                .collect(Collectors.toMap(TechnicianDetailResponseDTO::getTechnicianId,
                        t -> Optional.ofNullable(t.getTechnicianName())
                                     .orElse("Technician " + t.getTechnicianId()),
                        (a, b) -> a));

        Map<String, Long> technicianProductivity = requests.stream()
                .filter(r -> List.of("COMPLETED", "PAID").contains(r.getStatus()))
                .filter(r -> r.getTechnicianId() != null)
                .collect(Collectors.groupingBy(
                        r -> techIdToName.getOrDefault(r.getTechnicianId(),
                                                       "Technician " + r.getTechnicianId()),
                        Collectors.counting()));

        DashboardResponseDTO response = new DashboardResponseDTO();
        response.setTotalBookings(report.getTotalBookings());
        response.setRevenue(report.getRevenue());
        response.setCompletedServices(report.getCompletedServices());
        response.setCancelledServices(report.getCancelledServices());
        response.setPaymentSuccessRate(report.getPaymentSuccessRate());
        response.setAverageRating(report.getAverageRating());
        response.setServiceCompletionRate(report.getServiceCompletionRate());
        response.setGeneratedAt(report.getGeneratedAt());
        response.setTopTechnicians(topTechnicians);
        response.setCategoryDistribution(categoryDistribution);
        response.setRevenueByCategory(revenueByCategory);
        response.setTechnicianProductivity(technicianProductivity);

        response.setPendingServices((int) requests.stream()
                .filter(r -> "CREATED".equalsIgnoreCase(r.getStatus()))
                .count());

        response.setAssignedServices((int) requests.stream()
                .filter(r -> List.of("ASSIGNED", "ACCEPTED", "IN_PROGRESS")
                .contains(r.getStatus()))
                .count());

        // Failure rate
        long failedPayments = payments.stream()
                .filter(p -> "FAILED".equalsIgnoreCase(p.getPaymentStatus()))
                .count();

        double paymentFailureRate = payments.isEmpty()
                ? 0.0
                : ((double) failedPayments / payments.size()) * 100.0;

        response.setPaymentFailureRate(paymentFailureRate);

        return response;
    }
}
