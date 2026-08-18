package com.transport.tms.Trip.Lock.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Replaces XX10CLODSTOH (X3 SQL Server) — the LVS (Load Vehicle Stock)
 * header. Written on Validate ("LVS Create" in the UI), read by the
 * Route Management Detail screen's Vehicle Load Stock display — see
 * VrController.getLvs(). That table had 150+ columns; only the ones
 * that ever carried real (non-padding) data are kept.
 */
@Entity
@Table(name = "xr_lvsheader", schema = "tms")
@Getter
@Setter
public class LvsHeader {

    /** Generated as {SITE}{YY}{MM}XCHG{0000001}, same format as before. */
    @Id
    @Column(name = "lvs_number", length = 30)
    private String lvsNumber;

    @Column(name = "trip_code", nullable = false, unique = true)
    private String tripCode;

    @Column(name = "site")
    private String site;

    @Column(name = "arr_site")
    private String arrSite;

    @Column(name = "vehicle_code")
    private String vehicleCode;

    @Column(name = "driver_id")
    private String driverId;

    @Column(name = "doc_date")
    private LocalDate docDate;

    @Column(name = "departure_date")
    private LocalDate departureDate;

    @Column(name = "departure_time")
    private String departureTime;

    @Column(name = "arrival_date")
    private LocalDate arrivalDate;

    @Column(name = "arrival_time")
    private String arrivalTime;

    @Column(name = "capacity_weight")
    private Double capacityWeight;

    @Column(name = "total_cases")
    private Integer totalCases;

    /** 1 once "LVS Confirm" has succeeded (XX10CRESDH) — set by
     *  TripLockService.confirmLvs(), not at LVS creation time. */
    @Column(name = "confirmed_flag")
    private Integer confirmedFlag;

    /** 1 once "Load Truck" has succeeded (X10CSTKMTV) — set by
     *  TripLockService.loadTruck(), not at LVS creation time.
     *  BUG FIX: this used to be hardcoded to 1 in writeLvsHeader() at
     *  LVS *creation* time, which was wrong — it made every LVS look
     *  "loaded" the instant it was created, before the truck was ever
     *  actually loaded. */
    @Column(name = "load_flag")
    private Integer loadFlag;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;
}
