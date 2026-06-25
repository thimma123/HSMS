package com.hsms.analytics_service;

import com.hsms.analytics_service.entity.AnalyticsReport;
import com.hsms.analytics_service.feignclient.BookingServiceClient;
import com.hsms.analytics_service.feignclient.PaymentClient;
import com.hsms.analytics_service.feignclient.TechnicianClient;
import com.hsms.analytics_service.model.*;
import com.hsms.analytics_service.repository.AnalyticsReportRepository;
import com.hsms.analytics_service.service.AnalyticsServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AnalyticsServiceImplTest {

    @Mock
    private AnalyticsReportRepository repo;

    @Mock
    private BookingServiceClient bookingClient;

    @Mock
    private TechnicianClient technicianClient;

    @Mock
    private PaymentClient paymentClient;

    @InjectMocks
    private AnalyticsServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetDashboard_withValidData() {
        // Arrange: mock booking requests
        ServiceRequestDetailResponseDTO request = new ServiceRequestDetailResponseDTO();
        request.setRequestId(1L);
        request.setCategoryId(10L);
        request.setCategoryName("Electrical");
        request.setCity("Hyderabad");
        request.setTechnicianId(100L);
        request.setStatus("COMPLETED");
        request.setScheduledDateTime(LocalDateTime.now());

        when(bookingClient.getAllRequests()).thenReturn(List.of(request));

        // Mock technician
        TechnicianDetailResponseDTO technician = new TechnicianDetailResponseDTO();
        technician.setTechnicianId(100L);
        technician.setTechnicianName("John");
        technician.setRating(4.5);

        when(technicianClient.getAllTechnicians()).thenReturn(List.of(technician));

        // Mock payment
        PaymentResponseDTO payment = new PaymentResponseDTO();
        payment.setServiceRequestId(1L);
        payment.setPaymentStatus("SUCCESS");
        payment.setAmount(500.0);

        when(paymentClient.getAllPayments()).thenReturn(List.of(payment));

        // Mock repo save
        when(repo.save(any(AnalyticsReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        DashboardResponseDTO response = service.getDashboard(null, null, null, null, null, null);

        // Assert
        assertThat(response.getTotalBookings()).isEqualTo(1);
        assertThat(response.getRevenue()).isEqualTo(500.0);
        assertThat(response.getCompletedServices()).isEqualTo(1);
        assertThat(response.getPaymentSuccessRate()).isEqualTo(100.0);
        assertThat(response.getAverageRating()).isEqualTo(4.5);
        assertThat(response.getTopTechnicians()).hasSize(1);
        assertThat(response.getRevenueByCategory()).containsEntry("Electrical", 500.0);

        verify(repo, times(1)).save(any(AnalyticsReport.class));
        verify(bookingClient, times(1)).getAllRequests();
        verify(technicianClient, times(1)).getAllTechnicians();
        verify(paymentClient, times(1)).getAllPayments();
    }

    @Test
    void testGetDashboard_withNoData() {
        // Arrange empty lists
        when(bookingClient.getAllRequests()).thenReturn(List.of());
        when(technicianClient.getAllTechnicians()).thenReturn(List.of());
        when(paymentClient.getAllPayments()).thenReturn(List.of());

        when(repo.save(any(AnalyticsReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        DashboardResponseDTO response = service.getDashboard(null, null, null, null, null, null);

        // Assert
        assertThat(response.getTotalBookings()).isEqualTo(0);
        assertThat(response.getRevenue()).isEqualTo(0.0);
        assertThat(response.getPaymentSuccessRate()).isEqualTo(0.0);
        assertThat(response.getAverageRating()).isEqualTo(0.0);
        assertThat(response.getTopTechnicians()).isEmpty();
        assertThat(response.getRevenueByCategory()).isEmpty();

        verify(repo, times(1)).save(any(AnalyticsReport.class));
    }
}
