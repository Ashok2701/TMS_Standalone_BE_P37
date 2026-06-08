package com.transport.tms.UserManagement.Dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class UserResponseDTO {

    private UUID userId;

    private String username;

    private String fullName;

    private String email;

    private String mobileNo;

    private String role;

    private String userType;

    private List<String> sites;

    private Boolean active;
}