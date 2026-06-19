package com.transport.tms.RoutePlanner.Dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoutePlannerDriverDTO {

    private String driverId;

    private String driverName;

    private String employeeCode;

    private String mobileNo;

    private String licenseNumber;

    private Short licenseType;

    private Short driverStatus;

    private Boolean longHaulDriver;

    private Boolean allowAllVehicles;
}
