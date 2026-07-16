package com.transport.tms.Reports.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Dedicated report table for DriverReport. Fields are a starting point —
 * adjust once the DriverReport screen's exact columns are confirmed.
 */
@Entity
@Table(name = "rpt_driver_report")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "driver_id")
    private Long driverId;

    @Column(name = "driver_name")
    private String driverName;

    @Column(name = "total_deliveries")
    private Integer totalDeliveries;

    @Column(name = "distance_km")
    private Double distanceKm;

    @Column(name = "avg_per_trip_km")
    private Double avgPerTripKm;

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
