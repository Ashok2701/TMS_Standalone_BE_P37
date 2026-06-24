package com.transport.tms.Trip.Dto;

import lombok.Data;

@Data
public class TripStatusDTO {
    private String  optiStatus;  // Open | Optimised | Locked
    private Integer lockFlag;    // 1 = lock and push to X3
    private String  notes;
    private String  userCode;
}
