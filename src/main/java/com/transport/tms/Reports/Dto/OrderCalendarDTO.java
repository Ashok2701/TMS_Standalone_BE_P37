package com.transport.tms.Reports.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Response shape returned to the frontend for OrderCalendar. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCalendarDTO {
    private Long id;
    private Long orderId;
    private LocalDate orderDate;
    private String status;
    private String driverName;
    private String vehicleName;
    private LocalDate reportPeriodStart;
    private LocalDate reportPeriodEnd;
}
