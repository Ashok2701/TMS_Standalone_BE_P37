package com.transport.tms.UserManagement.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.transport.tms.UserManagement.Dto.UserAlignedSiteId;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "xx10cuserd")
@IdClass(UserAlignedSiteId.class)
public class UserAlignedSite{
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "xaus", referencedColumnName = "xaus", nullable = false)
    @JsonBackReference
    private User user;

    @Id
    @Column(name= "xfcy")
    private String fcy;
    @Column(name= "xemail")
    private String email;
    @Column(name= "xrole")
    private String role;
    @Column(name= "xphn")
    private String phone;
    @Column(name= "credattim")
    private Date credattim;
    @Column(name= "upddattim")
    private Date upddattim;
    @Column(name= "auuid")
    private byte[] auuid;
    @Column(name= "creusr")
    private String creusr;
    @Column(name= "updusr")
    private String updusr;
    @Column(name= "xrout")
    private Integer rout;
    @Column(name= "xmaps")
    private Integer map;
    @Column(name= "xdeffcy")
    private String defflg;
}
