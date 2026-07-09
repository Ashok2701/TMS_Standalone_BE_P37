package com.transport.tms.RoutePlanner.Dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RoutePlannerVehicleDTO {

    private String     vehicleCode;
    private String     vehicleName;
    private String     vehicleNumber;
    private String     categoryCode;
    private String     categoryDescription;
    private String     brand;
    private String     model;
    private Short      vehicleYear;
    private String     color;

    // Capacity
    private BigDecimal capacityWeight;
    private BigDecimal capacityVolume;
    private String     volumeUnit;
    private String     weightUnit;

    // Site / Depot
    private String     siteCode;           // site this vehicle belongs to
    private String     startDepot;         // departure depot
    private String     endDepot;           // arrival depot
    private String     earliestStartTime;  // HH:MM

    // Cost (used by VROOM)
    private BigDecimal fixedCost;
    private BigDecimal costPerTime;
    private BigDecimal costPerDistance;

    // Constraints
    private Short      maxOrderCount;
    private BigDecimal maxTotalTime;
    private BigDecimal maxTotalDistance;

    // Driver
    private String     driverId;

    // Image — Base64 encoded
    private String     image;

    // Status
    private Short      vehicleStatus;
    private Boolean    active;
}
