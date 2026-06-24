package com.transport.tms.Trip.Dto;

import lombok.Data;

@Data
public class TripStatusDTO {
    private String  status;       // Open | Optimised | Locked
    private Boolean locked;       // true = lock and push to X3
    private String  notes;
    private String  userCode;
}
