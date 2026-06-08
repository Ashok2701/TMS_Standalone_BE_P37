package com.transport.tms.UserManagement.Dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class UserRequestDTO {

    private String username;

    private String password;

    private String fullName;

    private String email;

    private String mobileNo;

    private UUID roleId;

    private UUID userTypeId;

    private List<String> sites;
}