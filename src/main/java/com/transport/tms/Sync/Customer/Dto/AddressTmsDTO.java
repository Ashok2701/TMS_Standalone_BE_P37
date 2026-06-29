package com.transport.tms.Sync.Customer.Dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class AddressTmsDTO {

    // ── Geo coordinates (per address) ─────────────────────────
    private BigDecimal latitude;
    private BigDecimal longitude;

    // ── Flags ─────────────────────────────────────────────────
    private Boolean anyTimeWindow;       // true = any time window applicable

    private Boolean anyVehicleCategory;  // true = any vehicle category eligible

    private Boolean anyDriver;           // true = any driver eligible

    // Grids
    private List<TimeWindowDTO> timeWindows;

    private List<AddressVehicleDTO> vehicles;

    private List<AddressDriverDTO> drivers;

    private String updatedBy;
}
