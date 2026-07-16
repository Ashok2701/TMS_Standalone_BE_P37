package com.transport.tms.Reports.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Response shape returned to the frontend for DriverReport. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverReportDTO {
    private Long id;
    private Long driverId;
    private String driverName;
    private Integer totalDeliveries;
    private Double distanceKm;
    private Double avgPerTripKm;
    private LocalDate reportPeriodStart;
    private LocalDate reportPeriodEnd;
}
