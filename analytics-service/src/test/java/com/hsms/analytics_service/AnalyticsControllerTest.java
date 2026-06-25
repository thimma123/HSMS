package com.hsms.analytics_service;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.hsms.analytics_service.controller.AnalyticsController;
import com.hsms.analytics_service.model.DashboardResponseDTO;
import com.hsms.analytics_service.service.AnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

class AnalyticsControllerTest {

    @Mock
    private AnalyticsService service;

    @InjectMocks
    private AnalyticsController controller;

    private DashboardResponseDTO mockResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockResponse = new DashboardResponseDTO();
        mockResponse.setTotalBookings(10);
        mockResponse.setRevenue(5000.0);
    }

    @Test
    void testDashboard_withAllParams() {
        // Arrange
        when(service.getDashboard("2024-01-01", "2024-12-31", 1L, "Hyderabad", 2L, "COMPLETED"))
                .thenReturn(mockResponse);

        // Act
        ResponseEntity<DashboardResponseDTO> response = controller.dashboard(
                "2024-01-01", "2024-12-31", 1L, "Hyderabad", 2L, "COMPLETED");

        // Assert
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo(mockResponse);
        verify(service, times(1))
                .getDashboard("2024-01-01", "2024-12-31", 1L, "Hyderabad", 2L, "COMPLETED");
    }

    @Test
    void testDashboard_withNoParams() {
        // Arrange
        when(service.getDashboard(null, null, null, null, null, null))
                .thenReturn(mockResponse);

        // Act
        ResponseEntity<DashboardResponseDTO> response = controller.dashboard(
                null, null, null, null, null, null);

        // Assert
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody().getTotalBookings()).isEqualTo(10);
        assertThat(response.getBody().getRevenue()).isEqualTo(5000.0);
        verify(service, times(1)).getDashboard(null, null, null, null, null, null);
    }
}
