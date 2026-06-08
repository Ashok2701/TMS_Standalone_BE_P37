package com.transport.tms.Sync.Customer.Dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Full Customer detail including TMS fields + list of addresses with their grids.
 * Used for GET single customer (detail view) and PUT update TMS fields.
 *
 * Structure:
 *   Customer (Info tab)
 *     ├── X3 read-only fields
 *     ├── TMS editable fields  (latitude, longitude, serviceTime, waitingTime)
 *     └── addresses[]          (Addresses tab)
 *           ├── X3 read-only fields
 *           ├── TMS flags       (anyTimeWindow, anyVehicleCategory, anyDriver)
 *           ├── timeWindows[]   (grid)
 *           ├── vehicles[]      (grid)
 *           └── drivers[]       (grid)
 */
@Getter
@Setter
public class CustomerDetailDTO {

    // ── X3 fields (read-only in UI) ───────────────────────────
    private String customerCode;
    private String customerName;
    private String shortName;
    private String countryCode;
    private String currencyCode;
    private Boolean active;
    private String syncedAt;

    // ── TMS fields (editable) ─────────────────────────────────
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String serviceTime;     // HH:MM
    private String waitingTime;     // HH:MM

    // ── Audit ─────────────────────────────────────────────────
    private String updatedBy;
    private LocalDateTime updatedAt;

    // ── Addresses tab ─────────────────────────────────────────
    private List<CustomerAddressDetailDTO> addresses;
}
