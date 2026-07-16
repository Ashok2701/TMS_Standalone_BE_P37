package com.transport.tms.Reports.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Response shape returned to the frontend for RouteList. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteListDTO {
    private Long id;
    private String routeName;
    private Integer tripCount;
    private Double totalDistanceKm;
    private String status;
    private LocalDate reportPeriodStart;
    private LocalDate reportPeriodEnd;
}
