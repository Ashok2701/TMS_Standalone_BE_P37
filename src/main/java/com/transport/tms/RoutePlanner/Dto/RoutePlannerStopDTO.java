package com.transport.tms.RoutePlanner.Dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class RoutePlannerStopDTO {

    // ── Identity ──────────────────────────────────────────────
    /** DROP or PICKUP */
    private String stopType;

    /** DLV = Delivery, PICK = Pick Ticket */
    private String docType;

    /** X3 document number */
    private String docNum;

    private String movType;

    // ── Site / Date ───────────────────────────────────────────
    private String site;

    private LocalDate docDate;

    private LocalDate originalDeliveryDate;

    private String companyCode;

    // ── Status ────────────────────────────────────────────────
    private Integer deliveryStatus;

    private String routeStatus;

    private Integer priority;

    // ── Route (x — from X3) ───────────────────────────────────
    private String routeCode;

    private String routeCodeDesc;

    private String routeCodeBgColor;

    // ── Route config (p — from Postgres xr_document_config) ───
    /** ROUTETAG    → xr_document_config.display_name_en */
    private String routeTag;

    /** ROUTETAGFRA → xr_document_config.display_name_fr */
    private String routeTagFra;

    /** ROUTECOLOR  → xr_document_config.color_code */
    private String routeColor;

    // ── Business partner (x — from X3) ────────────────────────
    private String bpCode;

    private String bpName;

    private String addressCode;

    private String addressName;

    // ── Address (x — from X3) ─────────────────────────────────
    private String addLine1;

    private String addLine2;

    private String addLine3;

    private String posCode;

    private String city;

    private String stateCode;

    private String countryCode;

    private String countryName;

    // ── Geo (p — from Postgres xr_customer) ───────────────────
    /** GPS_X → xr_customer.latitude */
    private BigDecimal latitude;

    /** GPS_Y → xr_customer.longitude */
    private BigDecimal longitude;

    // ── Weight / Volume (x — from X3) ─────────────────────────
    private BigDecimal nbPack;

    private BigDecimal netWeight;

    private String weightUnit;

    private BigDecimal volume;

    private String volumeUnit;

    // ── Driver / Vehicle (x — from X3) ────────────────────────
    private String driverCode;

    private String vehicleCode;

    private String vehiclePlate;

    // ── Trip / Plan (x — from X3) ─────────────────────────────
    private String tripNo;

    private String vrCode;

    private String vrSeq;

    private Integer seq;

    private String dlvMode;

    // ── Departure / Arrival (x — from X3) ─────────────────────
    private LocalDate depDate;

    private String depTime;

    private LocalDate arvDate;

    private String arvTime;

    // ── Carrier (x — from X3) ─────────────────────────────────
    private String carrier;

    private String carrColor;

    private String docInst;

    // ── Service / Waiting time (p — from Postgres xr_customer) ─
    /** SERVICETIME  → xr_customer.service_time */
    private String serviceTime;

    /** WaitingTime  → xr_customer.waiting_time */
    private String waitingTime;

    // ── Time windows (p — from Postgres xr_customer_address_timewindow)
    private Boolean anyTimeWindow;

    private String fromTime;

    private String toTime;
}
