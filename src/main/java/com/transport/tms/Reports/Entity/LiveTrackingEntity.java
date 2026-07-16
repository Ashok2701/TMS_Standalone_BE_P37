package com.transport.tms.Reports.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Dedicated report table for LiveTracking. Fields are a starting point —
 * adjust once the LiveTracking screen's exact columns are confirmed.
 */
@Entity
@Table(name = "rpt_live_tracking")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveTrackingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Column(name = "driver_name")
    private String driverName;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "speed_kmh")
    private Double speedKmh;

    @Column(name = "status")
    private String status;

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
