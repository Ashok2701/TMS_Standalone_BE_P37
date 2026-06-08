package com.transport.tms.Sync.Customer.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "xr_customer", schema = "tms")
@Getter
@Setter
public class XRCustomer {

    // ── X3 FIELDS (managed by sync, never edit manually) ──────
    @Id
    @Column(name = "customer_code")
    private String customerCode;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "short_name")
    private String shortName;

    @Column(name = "country_code")
    private String countryCode;

    @Column(name = "currency_code")
    private String currencyCode;

    @Column(name = "active")
    private Boolean active;

    @Column(name = "synced_at")
    private LocalDateTime syncedAt;

    // ── TMS FIELDS (managed via TMS UI, never touched by sync) ─
    @Column(name = "latitude")
    private BigDecimal latitude;

    @Column(name = "longitude")
    private BigDecimal longitude;

    @Column(name = "service_time")
    private String serviceTime;       // HH:MM

    @Column(name = "waiting_time")
    private String waitingTime;       // HH:MM

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
