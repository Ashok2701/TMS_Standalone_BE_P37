package com.transport.tms.UserManagement.Entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;


@Entity
@Table(name = "xr_user_types")
@Getter
@Setter
public class XRUserType {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_type_id")
    private UUID userTypeId;

    @Column(unique = true, name = "user_type_code")
    private String userTypeCode;

    @Column(name = "user_type_name")
    private String userTypeName;

    @Column(name = "requires_site_mapping")
    private Boolean requiresSiteMapping;

    private Boolean active = true;
}