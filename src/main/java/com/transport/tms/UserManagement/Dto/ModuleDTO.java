package com.transport.tms.UserManagement.Dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ModuleDTO {

    private UUID moduleId;

    private String moduleCode;

    private String moduleName;

    private String menuName;

    private String menuPath;

    private String icon;

    private Integer displayOrder;

    private Boolean active;
}