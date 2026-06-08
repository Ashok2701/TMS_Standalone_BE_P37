package com.transport.tms.Fleet.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class UnavailableDays {
    private Date startDate;
    private Date endDate;
    private String desc;
}
