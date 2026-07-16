package com.transport.tms.Reports.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Response shape returned to the frontend for LiveTracking. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveTrackingDTO {
    private Long id;
    private Long vehicleId;
    private String driverName;
    private Double latitude;
    private Double longitude;
    private Double speedKmh;
    private String status;
    private LocalDate reportPeriodStart;
    private LocalDate reportPeriodEnd;
}
