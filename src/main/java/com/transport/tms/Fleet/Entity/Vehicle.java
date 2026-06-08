package com.transport.tms.Fleet.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "xr_vehicle")
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
    @JoinColumn(
            name = "category_code",
            referencedColumnName = "category_code")
    private VehicleCategory category;

    @Column(name = "brand")
    private String brand;

    @Column(name = "model")
    private String model;

    @Column(name = "vehicle_year")
    private Short vehicleYear;

    @Column(name = "color")
    private String color;

    @Column(name = "capacity_weight")
    private BigDecimal capacityWeight;

    @Column(name = "capacity_volume")
    private BigDecimal capacityVolume;

    @Column(name = "volume_unit")
    private String volumeUnit;

    @Column(name = "weight_unit")
    private String weightUnit;

    @Column(name = "driver_id")
    private String driverId;

    @Column(name = "active")
    private Boolean active = true;

    @Column(name = "vehicle_status")
    private Short vehicleStatus;

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