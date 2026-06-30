package com.transport.tms.Sync.Customer.Dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Full address detail with TMS flags and all 3 grids.
 * Nested inside CustomerDetailDTO.addresses[]
 */
@Getter
@Setter
public class CustomerAddressDetailDTO {

    // ── X3 fields (read-only in UI) ───────────────────────────
    private String addressCode;
    private String customerCode;
    private String addressDescription;
    private String addressLine1;
    private String addressLine2;
    private String addressLine3;
    private String city;
    private String postalCode;
    private String stateCode;
    private String countryCode;
    private String countryName;
    private String phone;
    private String mobile;
    private String email;
    private String webSite;
    private Boolean defaultAddress;
    private LocalDateTime syncedAt;

    // ── TMS geo (editable) ─────────────────────────────────────
    private BigDecimal latitude;
    private BigDecimal longitude;

    // ── TMS flags (editable) ──────────────────────────────────
    private Boolean anyTimeWindow;      // true = visit at any time
    private Boolean anyVehicleCategory; // true = all vehicle types allowed
    private Boolean anyDriver;          // true = any driver allowed

    // ── TMS grids (editable) ──────────────────────────────────
    private List<TimeWindowDTO> timeWindows;
    private List<AddressVehicleDTO> vehicles;
    private List<AddressDriverDTO> drivers;

    // ── Audit ─────────────────────────────────────────────────
    private String updatedBy;
    private LocalDateTime updatedAt;
}
