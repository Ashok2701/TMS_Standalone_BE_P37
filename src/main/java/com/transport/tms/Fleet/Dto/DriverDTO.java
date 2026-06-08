package com.transport.tms.Fleet.Dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class DriverDTO {

    private String driverId;

    private String driverName;

    private Boolean active;

    private String employeeCode;

    private LocalDate dateOfBirth;

    private String mobileNo;

    private String alternateMobile;

    private String email;

    private String addressLine1;

    private String addressLine2;

    private String city;

    private String postalCode;

    private String countryCode;

    private String licenseNumber;

    private Short licenseType;

    private LocalDate licenseIssueDate;

    private LocalDate licenseExpiryDate;

    private String issuedBy;

    private LocalDate lastMedicalDate;

    private Integer maxHoursPerDay;

    private Integer maxHoursPerWeek;

    private Short driverStatus;

    private Boolean allowAllVehicles;

    private Boolean longHaulDriver;

    private String notes;
}