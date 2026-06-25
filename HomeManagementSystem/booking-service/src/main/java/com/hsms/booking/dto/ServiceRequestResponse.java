package com.hsms.booking.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hsms.booking.enums.ServiceRequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for service request response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Service request response data")
public class ServiceRequestResponse {

    @Schema(description = "Unique request identifier", example = "1")
    private Long requestId;

    @Schema(description = "Service category name", example = "Plumbing Repairs")
    private String categoryName;

    @Schema(description = "Service category base price", example = "500.00")
    private BigDecimal basePrice;

    @Schema(description = "Request status", example = "CREATED")
    private ServiceRequestStatus status;

    @Schema(description = "Service location address", example = "123 Main Street")
    private String address;

    @Schema(description = "City/area", example = "Mumbai")
    private String city;

    @Schema(description = "Postal code", example = "400001")
    private String pincode;

    @Schema(description = "When the service is scheduled", example = "2024-06-20T10:00:00")
    private LocalDateTime scheduledDateTime;

    @Schema(description = "Additional request details")
    private String description;

    @Schema(description = "Assigned technician name (if status >= ASSIGNED)", example = "John Technician")
    private String technicianName;

    @Schema(description = "Technician rating (if assigned)", example = "4.5")
    private Double technicianRating;

    @Schema(description = "When the request was created", example = "2024-06-14T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "When the request was last updated", example = "2024-06-14T11:30:00")
    private LocalDateTime updatedAt;

    @Schema(description = "Customer ID")
    private Long customerId;

    @Schema(description = "Customer name")
    private String customerName;

    @Schema(description = "Technician ID (if assigned)")
    private Long technicianId;
    
    private Long categoryId;
}