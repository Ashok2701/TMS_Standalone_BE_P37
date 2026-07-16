package com.transport.tms.Reports.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Dedicated report table for VehicleReport. Fields are a starting point —
 * adjust once the VehicleReport screen's exact columns are confirmed.
 */
@Entity
@Table(name = "rpt_vehicle_report")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Column(name = "vehicle_name")
    private String vehicleName;

    @Column(name = "total_trips")
    private Integer totalTrips;

    @Column(name = "distance_km")
    private Double distanceKm;

    @Column(name = "utilization_percent")
    private Double utilizationPercent;

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
