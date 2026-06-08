package com.transport.tms.transport.sqlserver.Dto;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SiteDTO {


    private String fcy;
    private String defflg;
    private String fcynam;
    private String cry;
    private String xx10c_geox;
    private String xx10c_geoy;
    private int fcyNumber;

}
