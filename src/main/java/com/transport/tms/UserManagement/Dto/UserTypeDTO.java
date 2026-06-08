package com.transport.tms.UserManagement.Dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UserTypeDTO {

    private UUID userTypeId;

    private String userTypeCode;

    private String userTypeName;

    private Boolean requiresSiteMapping;

    private Boolean active;
}
