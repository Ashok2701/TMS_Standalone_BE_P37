package com.transport.tms.Fleet.Dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class DriverDTO {

    private String driverId;

    private String driverName;

    // POD login credentials. username is safe to return in responses;
    // password is write-only — DriverServiceImpl.mapToDTO() explicitly
    // clears it before returning any Driver, so a hash never reaches
    // the frontend. Leave blank on update to keep the existing password.
    private String username;

    private String password;

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

    // Image — Base64 encoded for JSON transport
    private String image;

    // Audit
    private String        createdBy;
    private LocalDateTime createdAt;
    private String        updatedBy;
    private LocalDateTime updatedAt;
}