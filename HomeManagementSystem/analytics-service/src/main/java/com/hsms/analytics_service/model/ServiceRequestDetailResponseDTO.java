package com.hsms.analytics_service.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequestDetailResponseDTO {
    private Long requestId;
    private Long customerId;
    private Long categoryId;
    private Long technicianId;
    private String status;
    private String address;
    private LocalDateTime scheduledDateTime;
    private LocalDateTime createdAt;
    private Double basePrice;
    private String categoryName;
    private String city;
}
