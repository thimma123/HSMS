package com.hsms.userservice.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileResponseDTO {

    private Long userId;
    private String name;
    private String email;
}