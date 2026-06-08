package com.transport.tms.UserManagement.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "xr_modules")
@Getter
@Setter
public class XRModule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "module_id")
    private UUID moduleId;

    @Column(name = "module_code",
            nullable = false,
            unique = true)
    private String moduleCode;

    @Column(name = "module_name",
            nullable = false)
    private String moduleName;

    @Column(name = "menu_name")
    private String menuName;

    @Column(name = "menu_path")
    private String menuPath;

    @Column(name = "icon")
    private String icon;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "active")
    private Boolean active = true;
}