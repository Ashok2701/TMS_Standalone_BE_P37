package com.transport.tms.Reports.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Dedicated report table for RouteList. Fields are a starting point —
 * adjust once the RouteList screen's exact columns are confirmed.
 */
@Entity
@Table(name = "rpt_route_list")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteListEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "route_name")
    private String routeName;

    @Column(name = "trip_count")
    private Integer tripCount;

    @Column(name = "total_distance_km")
    private Double totalDistanceKm;

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
