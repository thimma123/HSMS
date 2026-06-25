package com.hsms.userservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hsms.userservice.enums.AvailabilityStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TechnicianDetailResponseDTO {

    @JsonProperty("technician_Id")
    private Long technicianId;
    private Long userId;
    private String name;
    private String email;

    private String skill;
    private Integer experience;
    private AvailabilityStatus availabilityStatus;
    private Double rating;
}
