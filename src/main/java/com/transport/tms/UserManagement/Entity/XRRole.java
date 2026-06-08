package com.transport.tms.UserManagement.Entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;


@Entity
@Table(name = "xr_roles")
@Getter
@Setter
public class XRRole {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "role_id")
    private UUID roleId;

    @Column(unique = true, name="role_code")
    private String roleCode;

    @Column(name = "role_name")
    private String roleName;

    private Boolean active = true;
}