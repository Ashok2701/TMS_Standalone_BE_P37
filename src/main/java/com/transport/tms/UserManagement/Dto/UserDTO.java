package com.transport.tms.UserManagement.Dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {
    private String xlogin;
    private String xusrname;
    private Boolean xact;
    private String email;
    private String lngmain;
    private String lansec;
    private int role;
}
