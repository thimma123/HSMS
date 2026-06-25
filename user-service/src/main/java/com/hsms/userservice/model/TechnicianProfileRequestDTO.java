	package com.hsms.userservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hsms.userservice.enums.AvailabilityStatus;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TechnicianProfileRequestDTO {

    @JsonProperty("technician_Id")
    private Long technicianId;

    private Long userId;

    @NotBlank(message = "Skill is required")
    private String skill;

    @NotNull(message = "Experience is required")
    @Min(value = 0, message = "Experience cannot be negative")
    private Integer experience;

    @NotNull(message = "Availability status is required")
    private AvailabilityStatus availabilityStatus;
}