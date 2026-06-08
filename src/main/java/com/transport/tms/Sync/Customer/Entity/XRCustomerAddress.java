package com.transport.tms.Sync.Customer.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "xr_customer_address", schema = "tms")
@Getter
@Setter
public class XRCustomerAddress {

    // ── X3 FIELDS (managed by sync, never edit manually) ──────
    @Id
    @Column(name = "address_code")
    private String addressCode;

    @Column(name = "customer_code")
    private String customerCode;

    @Column(name = "address_description")
    private String addressDescription;

    @Column(name = "address_line1")
    private String addressLine1;

    @Column(name = "address_line2")
    private String addressLine2;

    @Column(name = "address_line3")
    private String addressLine3;

    @Column(name = "city")
    private String city;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "state_code")
    private String stateCode;

    @Column(name = "country_code")
    private String countryCode;

    @Column(name = "country_name")
    private String countryName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "mobile")
    private String mobile;

    @Column(name = "email")
    private String email;

    @Column(name = "web_site")
    private String webSite;

    @Column(name = "default_address")
    private Boolean defaultAddress;

    @Column(name = "synced_at")
    private LocalDateTime syncedAt;

    // ── TMS FIELDS (managed via TMS UI, never touched by sync) ─
    @Column(name = "any_time_window")
    private Boolean anyTimeWindow = false;      // true = all time windows applicable

    @Column(name = "any_vehicle_category")
    private Boolean anyVehicleCategory = false; // true = all vehicle categories eligible

    @Column(name = "any_driver")
    private Boolean anyDriver = false;          // true = all drivers eligible

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── TMS GRID RELATIONS ──────────────────────────────────────
    @OneToMany(
            mappedBy = "address",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<XRAddressTimeWindow> timeWindows;

    @OneToMany(
            mappedBy = "address",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<XRAddressVehicle> vehicles;

    @OneToMany(
            mappedBy = "address",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<XRAddressDriver> drivers;
}
