package com.transport.tms.Sync.Site.Dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;


@Getter
@Setter
public class SiteUpdateDTO {


    private BigDecimal latitude;


    private BigDecimal longitude;


    private String workingStartTime;


    private String workingEndTime;


    private Integer loadingDockCount;


    private Integer maxVehicleCapacity;


    private Boolean tmsFlag;


    private String remarks;


    private String updatedBy;


}