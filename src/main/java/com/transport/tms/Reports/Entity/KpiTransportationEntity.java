package com.transport.tms.Reports.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Dedicated report table for KpiTransportation. Fields are a starting point —
 * adjust once the KpiTransportation screen's exact columns are confirmed.
 */
@Entity
@Table(name = "rpt_kpi_transportation")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KpiTransportationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kpi_name")
    private String kpiName;

    @Column(name = "kpi_value")
    private Double kpiValue;

    @Column(name = "unit")
    private String unit;

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
