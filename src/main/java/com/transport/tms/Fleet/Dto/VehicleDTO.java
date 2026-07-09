package com.transport.tms.Fleet.Dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class VehicleDTO {

    private String     vehicleCode;
    private String     vehicleName;
    private String     vehicleNumber;
    private String     categoryCode;
    private String     categoryDescription;

    private String     brand;
    private String     model;
    private Short      vehicleYear;
    private String     color;
    private Short      fuelType;
    private Short      engineCc;
    private String     chassisNumber;

    // Capacity
    private BigDecimal capacityWeight;
    private BigDecimal capacityVolume;
    private String     volumeUnit;
    private String     weightUnit;

    // Site / Depot
    private String     siteCode;
    private String     startDepot;
    private String     endDepot;
    private String     arrivalDeparture;

    // Timing
    private String     earliestStartTime;
    private BigDecimal maxTotalTime;
    private BigDecimal maxTotalTravel;
    private BigDecimal maxTotalDistance;
    private Short      maxOrderCount;

    // Cost
    private BigDecimal fixedCost;
    private BigDecimal costPerTime;
    private BigDecimal costPerDistance;
    private BigDecimal overtimeStart;
    private BigDecimal overtimeCost;

    // Dimensions
    private BigDecimal vehicleLength;
    private BigDecimal vehicleWidth;
    private BigDecimal vehicleHeight;
    private BigDecimal emptyMass;
    private BigDecimal grossMass;
    private BigDecimal tolerance;

    // Driver / Trailer
    private String     driverId;
    private String     trailerNumber;
    private Boolean    allowAllDrivers;

    // Tracking
    private String     gpsId;
    private String     mobileTracker;
    private Integer    odometer;
    private BigDecimal currentMeter;

    // Licensing
    private String        licenseReference;
    private LocalDateTime licenseExpiry;
    private String        insuranceReference;
    private LocalDateTime insuranceExpiry;
    private LocalDateTime lastInspectionDate;
    private LocalDateTime inspectionExpiry;

    // Other
    private String  specialtyName;
    private String  assignmentRule;
    private String  equipmentNotes;
    private Boolean externalVehicle;

    // Image — Base64 encoded for JSON transport
    private String  image;

    // Status
    private Boolean active;
    private Short   vehicleStatus;

    // Audit
    private String        createdBy;
    private LocalDateTime createdAt;
    private String        updatedBy;
    private LocalDateTime updatedAt;
}
