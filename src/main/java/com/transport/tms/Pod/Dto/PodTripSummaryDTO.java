package com.transport.tms.Pod.Dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PodTripSummaryDTO {

    private String tripCode;
    private String site;
    private String vehicleCode;
    private String driverName;
    private String status;      // Open | Optimised | Locked | Validated
    private String depSite;
    private String arrSite;
    private String startTime;
    private Integer stops;
    private Integer drops;
    private Integer pickups;
}
