package com.hsms.authservice.model;

import java.util.Set;

import com.hsms.authservice.entity.Role;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileDTO {

    private Long userId;
    private String name;
    private String email;
    private Set<Role> role;
	
}