package com.hsms.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for creating a new service request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Service request creation request")
public class ServiceRequestDTO {

    @NotNull(message = "Category ID is required")
    @Schema(description = "Service category ID", example = "1")
    private Long categoryId;

    @NotBlank(message = "Address is required")
    @Schema(description = "Service location address", example = "123 Main Street, Apt 4B")
    private String address;

    @NotNull(message = "Scheduled date/time is required")
    @Future(message = "Scheduled date/time must be in the future")
    @Schema(description = "When the service is requested (must be future)", example = "2024-06-20T10:00:00")
    private LocalDateTime scheduledDateTime;

    @Schema(description = "Additional details about the service request", example = "Need emergency repair")
    private String description;

    @Schema(description = "Preferred city/area", example = "Mumbai")
    private String city;

    @Schema(description = "Postal code", example = "400001")
    private String pincode;
}