package com.hsms.execution_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceStartedEvent {
    private Long serviceRequestId;
    private Long customerId;
    private Long technicianId;
}
