package com.transport.tms.RoutePlanner.Dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RoutePlannerVehicleDTO {

    private String vehicleCode;

    private String vehicleName;

    private String vehicleNumber;

    private String categoryCode;

    private String categoryDescription;

    private String brand;

    private String model;

    private Short vehicleYear;

    private String color;

    private BigDecimal capacityWeight;

    private BigDecimal capacityVolume;

    private String volumeUnit;

    private String weightUnit;

    private Short vehicleStatus;

    // Assigned driver (from xr_vehicle.driver_id)
    private String driverId;

    // Site/facility this vehicle belongs to
    private String site;
}
