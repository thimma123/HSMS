package com.hsms.execution_service.model;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class BookingServiceResponseDTO {

    private Long requestId;
    private String categoryName;
    private Double basePrice;

    private String status;

    private String address;
    private String city;
    private String pincode;
    private LocalDateTime scheduledDateTime;
    private String description;

    private String technicianName;
    private Double technicianRating;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long customerId;
    private String customerName;

    private Long technicianId;
    private Long categoryId;
}