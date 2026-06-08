package com.transport.tms.UserManagement.Entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name="xx10cusers")
public class User {
    @Id
    @JsonProperty("username")
    @Column(name= "xaus")
    private String xlogin;
    @JsonProperty("password")
    @Column(name= "xpwd")
    private String xpswd;
    @Column(name= "xausna")
    private String xusrname;
    @Column(name= "credattim")
    private Date credattim;
    @Column(name= "upddattim")
    private Date upddattim;
    @Column(name= "auuid")
    private UUID auuid;
    @Column(name= "creusr")
    private String creusr;
    @Column(name= "updusr")
    private String updusr;
    @Column(name= "xact")
    private Boolean xact;
    @Column(name= "xemail")
    private String email;
    @Column(name= "xphn")
    private String phone;
    @Column(name= "xlanmain")
    private String lngmain;
    @Column(name= "xlansec")
    private String lansec;
    @Column(name= "bpaaddlig")
    private String bpadd;
    @Column(name= "bpaaddlig1")
    private String bpadd1;
    @Column(name= "bpaaddlig2")
    private String bpadd2;
    @Column(name= "poscode")
    private String pincode;
    @Column(name= "cty")
    private String city;
    @Column(name= "xtel")
    private String tel;
    @Column(name= "sat")
    private String state;
    @Column(name= "cry")
    private String country;
    @Column(name= "xrpflg")
    private Boolean routeplannerflg;
    @Column(name= "xschflg")
    private Boolean schedulerflg;
    @Column(name= "xcalvflg")
    private Boolean calendarrpflg;
    @Column(name= "xmapvflg")
    private Boolean mapviewrpflg;
    @Column(name= "xscrrtflg")
    private Boolean screportsflg;
    @Column(name= "xfleetflg")
    private Boolean fleetmgmtflg;
    @Column(name= "xusrmgmtflg")
    private Boolean usermgmtflg;
    @Column(name= "xrmpckflg")
    private Boolean removePicktcktflg;
    @Column(name= "xaddpckflg")
    private Boolean addPicktcktflg;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<UserAlignedSite> alignedSites;
}

