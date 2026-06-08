package com.transport.tms.Fleet.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class TechnicalInspection {
    private String inspectionType;
    private Date lastCheck;
    private Integer periodicity;
    private Date nextVisit;
    private Integer type;
}
