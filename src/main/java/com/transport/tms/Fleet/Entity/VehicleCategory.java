package com.transport.tms.Fleet.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "xr_vehicle_category")
@Getter
@Setter
public class VehicleCategory {

    @Id
    @Column(name = "category_code")
    private String categoryCode;

    @Column(name = "description")
    private String description;

    @Column(name = "is_active")
    private Boolean active = true;

    @Column(name = "country_code")
    private String countryCode;

    @Column(name = "vehicle_type")
    private Short vehicleType;

    @Column(name = "axle_count")
    private Short axleCount;

    @Column(name = "max_capacity_wt")
    private BigDecimal maxCapacityWt;

    @Column(name = "max_capacity_vol")
    private BigDecimal maxCapacityVol;

    @Column(name = "volume_unit")
    private String volumeUnit;

    @Column(name = "weight_unit")
    private String weightUnit;

    @Column(name = "skill_number")
    private Short skillNumber;

    @Column(name = "inspection_in")
    private String inspectionIn;

    @Column(name = "manual_in")
    private Short manualIn;

    @Column(name = "inspection_out")
    private String inspectionOut;

    @Column(name = "manual_out")
    private Short manualOut;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}