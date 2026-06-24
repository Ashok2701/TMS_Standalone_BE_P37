package com.transport.tms.RoutePlanner.Dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * Product line for a single stop (DROP or PICKUP).
 * One stop can have multiple product lines.
 * These are embedded inside stopObjects JSONB as a "products" array.
 */
@Data
public class StopProductDTO {

    private String     docNum;       // parent document number
    private Integer    lineNum;      // line sequence number
    private String     cpyCode;

    // Product identity
    private String     itemCode;     // ITMREF — product code
    private String     itemDesc1;    // ITMDES1 — description line 1
    private String     itemDesc2;    // ITMDES2 — description line 2

    // Quantities
    private BigDecimal qtyOrdered;   // ordered quantity
    private BigDecimal qtyStockUnit; // in stock unit
    private BigDecimal qtyDelivered; // delivered quantity
    private String     stockUnit;    // STU e.g. "EA", "KG"
    private String     packUnit;     // PCU e.g. "CS", "PAL"

    // Weight / Volume per line
    private BigDecimal netWeight;
    private BigDecimal grossWeight;
    private BigDecimal volume;
    private String     weightUnit;
    private String     volumeUnit;

    // Lot / Serial / Packing
    private String     lot;
    private String     serial;
    private String     packNum;

    // Site + Status
    private String     site;
    private String     lineStatus;

    // Stop type — filled by service
    private String     stopType;     // "DROP" | "PICKUP"
}
