package com.transport.tms.Sync.Customer.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "xr_customer_address", schema = "tms")
@Getter
@Setter
public class XRCustomerAddress {

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
}
