package com.hsms.booking.enums;

/**
 * Enumeration for service request status
 */
public enum ServiceRequestStatus {
    CREATED("Created"),
    ASSIGNED("Assigned"),
    ACCEPTED("Accepted"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    PAID("Paid"),
    CANCELLED("Cancelled"),
    REJECTED("Rejected"),
    REASSIGNED("Reassigned");

    private final String displayName;

    ServiceRequestStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
