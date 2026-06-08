package com.transport.tms.UserManagement.Dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class LoginResponseDTO {
    private String accessToken;

    private String username;

    private String fullName;

    private String role;

    private String userType;

    private List<String> sites;

    private List<PermissionDTO> permissions;


}
