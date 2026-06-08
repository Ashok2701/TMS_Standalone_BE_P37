package com.transport.tms.UserManagement.Dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionDTO {

    private String moduleCode;

    private String moduleName;

    private String menuPath;

    private Boolean canView;

    private Boolean canCreate;

    private Boolean canEdit;

    private Boolean canDelete;

}
