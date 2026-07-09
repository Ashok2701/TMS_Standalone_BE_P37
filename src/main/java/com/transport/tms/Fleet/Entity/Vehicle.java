package com.transport.tms.Fleet.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "xr_vehicle", schema = "tms")
@Getter
@Setter
public class Vehicle {

    @Id
    @Column(name = "vehicle_code")
    private String vehicleCode;

    @Column(name = "vehicle_name")
    private String vehicleName;

    @Column(name = "vehicle_number")
    private String vehicleNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_code", referencedColumnName = "category_code")
    private VehicleCategory category;

    @Column(name = "brand")
    private String brand;

    @Column(name = "model")
    private String model;

    @Column(name = "vehicle_year")
    private Short vehicleYear;

    @Column(name = "color")
    private String color;

    @Column(name = "fuel_type")
    private Short fuelType;

    @Column(name = "engine_cc")
    private Short engineCc;

    @Column(name = "chassis_number")
    private String chassisNumber;

    @Column(name = "capacity_weight")
    private BigDecimal capacityWeight;

    @Column(name = "capacity_volume")
    private BigDecimal capacityVolume;

    @Column(name = "volume_unit")
    private String volumeUnit;

    @Column(name = "weight_unit")
    private String weightUnit;

    // ── Site / depot ──────────────────────────────────────────
    @Column(name = "site")
    private String site;          // site (xr_site.site_code)

    @Column(name = "departure_site")
    private String departureSite;        // departure site/site

    @Column(name = "arrival_site")
    private String arrivalSite;          // arrival site/site

    @Column(name = "arrival_departure")
    private String arrivalDeparture;  // arrival/departure config

    // ── Timing ───────────────────────────────────────────────
    @Column(name = "earliest_start_time")
    private String earliestStartTime; // HH:MM e.g. "07:00"

    @Column(name = "max_total_time")
    private BigDecimal maxTotalTime;

    @Column(name = "max_total_travel")
    private BigDecimal maxTotalTravel;

    @Column(name = "max_total_distance")
    private BigDecimal maxTotalDistance;

    @Column(name = "max_order_count")
    private Short maxOrderCount;

    // ── Cost ─────────────────────────────────────────────────
    @Column(name = "fixed_cost")
    private BigDecimal fixedCost;

    @Column(name = "cost_per_time")
    private BigDecimal costPerTime;

    @Column(name = "cost_per_distance")
    private BigDecimal costPerDistance;

    @Column(name = "overtime_start")
    private BigDecimal overtimeStart;

    @Column(name = "overtime_cost")
    private BigDecimal overtimeCost;

    // ── Dimensions ───────────────────────────────────────────
    @Column(name = "vehicle_length")
    private BigDecimal vehicleLength;

    @Column(name = "vehicle_width")
    private BigDecimal vehicleWidth;

    @Column(name = "vehicle_height")
    private BigDecimal vehicleHeight;

    @Column(name = "empty_mass")
    private BigDecimal emptyMass;

    @Column(name = "gross_mass")
    private BigDecimal grossMass;

    @Column(name = "tolerance")
    private BigDecimal tolerance;

    // ── Driver / Trailer ─────────────────────────────────────
    @Column(name = "driver_id")
    private String driverId;

    @Column(name = "trailer_number")
    private String trailerNumber;

    @Column(name = "allow_all_drivers")
    private Boolean allowAllDrivers;

    // ── Tracking ─────────────────────────────────────────────
    @Column(name = "gps_id")
    private String gpsId;

    @Column(name = "mobile_tracker")
    private String mobileTracker;

    @Column(name = "odometer")
    private Integer odometer;

    @Column(name = "current_meter")
    private BigDecimal currentMeter;

    // ── Licensing / Inspection ───────────────────────────────
    @Column(name = "license_reference")
    private String licenseReference;

    @Column(name = "license_expiry")
    private LocalDateTime licenseExpiry;

    @Column(name = "insurance_reference")
    private String insuranceReference;

    @Column(name = "insurance_expiry")
    private LocalDateTime insuranceExpiry;

    @Column(name = "last_inspection_date")
    private LocalDateTime lastInspectionDate;

    @Column(name = "inspection_expiry")
    private LocalDateTime inspectionExpiry;

    // ── Other ────────────────────────────────────────────────
    @Column(name = "specialty_name")
    private String specialtyName;

    @Column(name = "assignment_rule")
    private String assignmentRule;

    @Column(name = "equipment_notes", columnDefinition = "TEXT")
    private String equipmentNotes;

    @Column(name = "external_vehicle")
    private Boolean externalVehicle;

    @Column(name = "vehicle_image")
    private byte[] vehicleImage;      // binary photo — BYTEA

    // ── Status / Flags ───────────────────────────────────────
    @Column(name = "active")
    private Boolean active = true;

    @Column(name = "vehicle_status")
    private Short vehicleStatus;

    // ── Audit ─────────────────────────────────────────────────
    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "uuid")
    private UUID uuid;
}
