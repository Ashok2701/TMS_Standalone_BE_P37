package com.transport.tms.UserManagement.Dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDetailDTO {
    private String xfcy;
    private String xrole;
    private String xemail;
    private String xphn;

    private Boolean xrout;
    private Boolean xmaps;
    private Boolean xdeffcy;
}
