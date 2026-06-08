package com.transport.tms.UserManagement.Dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserVO {
    private String xusrcode;
    @JsonProperty("username")
    private String xlogin;
    @JsonProperty ("password")
    private String xpswd;

    private String xusrname;
    private Boolean xact;
    private Date credattim;
    private Date upddattim;
    private UUID auuid;
    private String creusr;
    private String updusr;
    private BigDecimal rowid;
    private String accessToken;
    private Boolean routeplannerflg;
    private Boolean schedulerflg;
    private Boolean calendarrpflg;
    private Boolean mapviewrpflg;
    private Boolean screportsflg;
    private Boolean fleetmgmtflg;
    private Boolean usermgmtflg;
    private Boolean addPicktcktflg;
    private Boolean removePicktcktflg;

}
