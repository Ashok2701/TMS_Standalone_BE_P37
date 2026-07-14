package com.transport.tms.Fleet.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "xr_driver", schema = "tms")
@Getter
@Setter
public class Driver {

    @Id
    @Column(name = "driver_id")
    private String driverId;

    @Column(name = "driver_name")
    private String driverName;

    @Column(name = "site_code")
    private String site;          // site (xr_site.site_code)

    @Column(name = "active")
    private Boolean active = true;

    @Column(name = "employee_code")
    private String employeeCode;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "mobile_no")
    private String mobileNo;

    @Column(name = "alternate_mobile")
    private String alternateMobile;

    @Column(name = "email")
    private String email;

    @Column(name = "address_line1")
    private String addressLine1;

    @Column(name = "address_line2")
    private String addressLine2;

    @Column(name = "city")
    private String city;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "country_code")
    private String countryCode;

    @Column(name = "license_number")
    private String licenseNumber;

    @Column(name = "license_type")
    private Short licenseType;

    @Column(name = "license_issue_date")
    private LocalDate licenseIssueDate;

    @Column(name = "license_expiry_date")
    private LocalDate licenseExpiryDate;

    @Column(name = "issued_by")
    private String issuedBy;

    @Column(name = "last_medical_date")
    private LocalDate lastMedicalDate;

    @Column(name = "max_hours_per_day")
    private Integer maxHoursPerDay;

    @Column(name = "max_hours_per_week")
    private Integer maxHoursPerWeek;

    @Column(name = "driver_status")
    private Short driverStatus;

    @Column(name = "allow_all_vehicles")
    private Boolean allowAllVehicles;

    @Column(name = "long_haul_driver")
    private Boolean longHaulDriver;

    @Column(name = "notes")
    private String notes;

    @Column(name = "driver_image")
    private byte[] driverImage;  // binary photo — BYTEA

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