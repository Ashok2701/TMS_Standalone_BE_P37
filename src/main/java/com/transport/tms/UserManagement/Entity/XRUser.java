package com.transport.tms.UserManagement.Entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "xr_users")
@Getter
@Setter
public class XRUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "username",
            nullable = false,
            unique = true)
    private String username;

    @Column(name = "password",
            nullable = false)
    private String password;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "email")
    private String email;

    @Column(name = "mobile_no")
    private String mobileNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private XRRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_type_id")
    private XRUserType userType;

    @Column(name = "active")
    private Boolean active = true;

}