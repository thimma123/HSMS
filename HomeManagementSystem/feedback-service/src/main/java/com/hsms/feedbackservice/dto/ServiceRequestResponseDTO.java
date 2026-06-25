package com.hsms.feedbackservice.dto;

public class ServiceRequestResponseDTO {
    private Long requestId;
    private String status;
    private Long customerId;
    private Long technicianId;

    public ServiceRequestResponseDTO() {
    }

    public ServiceRequestResponseDTO(Long requestId, String status) {
        this.requestId = requestId;
        this.status = status;
    }

    public ServiceRequestResponseDTO(Long requestId, String status, Long customerId) {
        this.requestId = requestId;
        this.status = status;
        this.customerId = customerId;
    }

    public ServiceRequestResponseDTO(Long requestId, String status, Long customerId, Long technicianId) {
        this.requestId = requestId;
        this.status = status;
        this.customerId = customerId;
        this.technicianId = technicianId;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(Long technicianId) {
        this.technicianId = technicianId;
    }
}
