package com.hsms.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceEvent {
    private String eventType;
    private Long requestId;
    private Long customerId;
    private Long technicianId;
    private String message;
}