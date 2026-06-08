package com.transport.tms.Sync.Customer.Dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Lightweight customer row for list/table view.
 * Does NOT include addresses (too heavy for list).
 */
@Getter
@Setter
public class CustomerListDTO {

    private String customerCode;
    private String customerName;
    private String shortName;
    private String countryCode;
    private String currencyCode;
    private Boolean active;

    // TMS fields shown in list
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String serviceTime;
    private String waitingTime;

    // How many addresses this customer has
    private Integer addressCount;
}
