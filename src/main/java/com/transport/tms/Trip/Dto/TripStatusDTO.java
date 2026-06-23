package com.transport.tms.Trip.Dto;

import lombok.Data;

@Data
public class TripStatusDTO {
    private String  optiStatus;   // Open / Optimised / Locked / Validated
    private Integer lockFlag;
    private String  notes;
    private String  userCode;
}
