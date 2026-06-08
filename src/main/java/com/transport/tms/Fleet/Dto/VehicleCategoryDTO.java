package com.transport.tms.Fleet.Dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class VehicleCategoryDTO {

    private String categoryCode;

    private String description;

    private Boolean active;

    private String countryCode;

    private Short vehicleType;

    private Short axleCount;

    private BigDecimal maxCapacityWt;

    private BigDecimal maxCapacityVol;

    private String volumeUnit;

    private String weightUnit;

    private Short skillNumber;

    private String inspectionIn;

    private Short manualIn;

    private String inspectionOut;

    private Short manualOut;
}