package com.transport.tms.UserManagement.Dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RoleDTO {
    private String roleCode;
    private UUID roleId;
    private String roleName;
    private Boolean active;
}
