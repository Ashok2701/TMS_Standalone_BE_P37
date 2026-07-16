package com.transport.tms.Reports.Dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class VehicleReportFilterDTO {
    private LocalDate startDate;
    private LocalDate endDate;
    private String site; // optional - null means all sites
}