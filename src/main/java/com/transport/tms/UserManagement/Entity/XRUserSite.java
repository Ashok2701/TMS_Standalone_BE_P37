
package com.transport.tms.UserManagement.Entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "xr_user_sites")
@Getter
@Setter
public class XRUserSite {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_site_id")
    private UUID userSiteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private XRUser user;

    @Column(name = "site_code")
    private String siteCode;

    @Column(name = "active")
    private Boolean active = true;


}