package com.transport.tms.Reports.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Response shape returned to the frontend for PodTracking. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PodTrackingDTO {
    private Long id;
    private Long tripId;
    private Long orderId;
    private String driverName;
    private String status;
    private LocalDateTime deliveredAt;
    private LocalDate reportPeriodStart;
    private LocalDate reportPeriodEnd;
}
