package com.transport.tms.Reports.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Dedicated report table for PodTracking. Fields are a starting point —
 * adjust once the PodTracking screen's exact columns are confirmed.
 */
@Entity
@Table(name = "rpt_pod_tracking")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PodTrackingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trip_id")
    private Long tripId;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "driver_name")
    private String driverName;

    @Column(name = "status")
    private String status;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

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
