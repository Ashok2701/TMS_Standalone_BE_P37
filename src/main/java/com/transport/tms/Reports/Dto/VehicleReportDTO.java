package com.transport.tms.Reports.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleReportDTO {
    private String plate;
    private Long trips;
    private Double distance;
    private Double utilization;
}