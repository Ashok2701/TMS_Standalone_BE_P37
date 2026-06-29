package com.transport.tms.Sync.Customer.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "xr_customer_address", schema = "tms")
@IdClass(XRCustomerAddressId.class)
@Getter
@Setter
public class XRCustomerAddress {

    // ── COMPOSITE PK (customerCode + addressCode) ─────────────
    // In X3, BPAADD_0 (address code) is NOT globally unique —
    // values like "10", "001" repeat across customers.
    // The unique key is always (customer_code, address_code).
    @Id
    @Column(name = "customer_code")
    private String customerCode;

    @Id
    @Column(name = "address_code")
    private String addressCode;

    // ── X3 FIELDS (managed by sync) ───────────────────────────
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

    @Column(name = "active")
    private Boolean active = true;   // false = deleted in X3, kept for history

    // ── TMS FIELDS (managed via TMS UI, never touched by sync) ─

    // Geo coordinates — per address (each delivery address has its own lat/lon)
    @Column(name = "latitude",  precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "any_time_window")
    private Boolean anyTimeWindow = false;

    @Column(name = "any_vehicle_category")
    private Boolean anyVehicleCategory = false;

    @Column(name = "any_driver")
    private Boolean anyDriver = false;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── TMS GRID RELATIONS ─────────────────────────────────────
    // FK in child tables now references composite PK via address_code + customer_code
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
