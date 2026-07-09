package com.transport.tms.Fleet.Dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class VehicleDTO {

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

    private String driverId;

    private String site;
    private String departureSite;
    private String arrivalSite;    private String startTime;
    private Integer maxPallets;
    private Integer maxCases;

    private Boolean active;

    private Short vehicleStatus;
}