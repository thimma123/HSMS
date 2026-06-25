package com.hsms.userservice.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerDetailResponseDTO {

    private Long userId;
    private String name;
    private String email;

    private String address;
    private String city;
    private String pincode;
    private LocalDateTime createdAt;
}
