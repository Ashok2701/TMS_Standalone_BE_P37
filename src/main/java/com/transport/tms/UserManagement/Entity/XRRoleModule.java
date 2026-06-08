package com.transport.tms.UserManagement.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "xr_role_modules")
@Getter
@Setter
public class XRRoleModule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "role_module_id")
    private UUID roleModuleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private XRRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id")
    private XRModule module;

    @Column(name = "can_view")
    private Boolean canView = false;

    @Column(name = "can_create")
    private Boolean canCreate = false;

    @Column(name = "can_edit")
    private Boolean canEdit = false;

    @Column(name = "can_delete")
    private Boolean canDelete = false;
}