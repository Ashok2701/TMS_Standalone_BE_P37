package com.transport.tms.Reports.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Dedicated report table for OrderCalendar. Fields are a starting point —
 * adjust once the OrderCalendar screen's exact columns are confirmed.
 */
@Entity
@Table(name = "rpt_order_calendar")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCalendarEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "order_date")
    private LocalDate orderDate;

    @Column(name = "status")
    private String status;

    @Column(name = "driver_name")
    private String driverName;

    @Column(name = "vehicle_name")
    private String vehicleName;

    @Column(name = "report_period_start")
    private LocalDate reportPeriodStart;

    @Column(name = "report_period_end")
    private LocalDate reportPeriodEnd;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
