package com.transport.tms.UserManagement.Dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RoleModuleDTO {

    private UUID moduleId;

    private String moduleName;

    private Boolean canView;

    private Boolean canCreate;

    private Boolean canEdit;

    private Boolean canDelete;
}