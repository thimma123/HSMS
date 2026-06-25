package com.hsms.booking.client;

import lombok.Data;

@Data
public class TechnicianDTO {

    private Long userId;

    private String name;

    private String email;

    private String skill;

    private Integer experience;

    private String availabilityStatus;

    private Double rating;
}