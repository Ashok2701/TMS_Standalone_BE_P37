package com.transport.tms.Trip.Lock.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Replaces XX10CPLANCHA (X3 SQL Server). Written on Lock, read by the
 * Route Management Detail screen's "Route Information" panel — see
 * VrController.getVr(). Only the fields that ever carried real data in
 * the old table are kept; that table had 60+ columns that were always
 * hardcoded empty/zero (X3 schema padding TMS never used).
 */
@Entity
@Table(name = "xr_vrheader", schema = "tms")
@Getter
@Setter
public class VrHeader {

    @Id
    @Column(name = "trip_code", length = 60)
    private String tripCode;

    @Column(name = "vehicle_code")
    private String vehicleCode;

    @Column(name = "driver_id")
    private String driverId;

    @Column(name = "site")
    private String site;

    @Column(name = "arr_site")
    private String arrSite;

    @Column(name = "start_time")
    private String startTime;

    @Column(name = "end_time")
    private String endTime;

    @Column(name = "doc_date")
    private LocalDate docDate;

    @Column(name = "total_distance")
    private Double totalDistance;

    @Column(name = "total_time")
    private Double totalTime;

    @Column(name = "total_cost")
    private String totalCost;

    @Column(name = "travel_time")
    private String travelTime;

    @Column(name = "total_cases")
    private Integer totalCases;

    /** Mirrors what OPTIMSTA_0/DISPSTAT_0/XVALID_0/XSTATUS_0 always were
     *  set to (1) on the old table — kept as a single status flag rather
     *  than four always-identical columns. */
    @Column(name = "status")
    private Integer status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;
}
