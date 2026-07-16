package com.transport.tms.Reports.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Response shape returned to the frontend for KpiTransportation. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KpiTransportationDTO {
    private Long id;
    private String kpiName;
    private Double kpiValue;
    private String unit;
    private LocalDate reportPeriodStart;
    private LocalDate reportPeriodEnd;
}
