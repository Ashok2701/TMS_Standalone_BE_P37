package com.transport.tms.RoutePlanner.Repository;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Entity mapped to tms.vw_rp_stop_enrich (Postgres).
 * Provides all (p) fields for Route Planner stop enrichment.
 * Keyed by (doc_type + customer_code + address_code).
 */
@Entity
@Table(name = "vw_rp_stop_enrich", schema = "tms")
@IdClass(StopEnrichment.StopEnrichmentId.class)
@Getter
@Setter
public class StopEnrichment {

    // ── Composite PK ──────────────────────────────────────────
    @Id
    @Column(name = "doc_type")
    private String docType;           // 'DLV' or 'PICK'

    @Id
    @Column(name = "customer_code")
    private String customerCode;      // matches X3 BPCODE

    @Id
    @Column(name = "address_code")
    private String addressCode;       // matches X3 ADRESCODE

    // ── Geo (p) → xr_customer ─────────────────────────────────
    @Column(name = "latitude")
    private BigDecimal latitude;

    @Column(name = "longitude")
    private BigDecimal longitude;

    // ── Service / waiting time (p) → xr_customer ──────────────
    @Column(name = "service_time")
    private String serviceTime;

    @Column(name = "waiting_time")
    private String waitingTime;

    // ── Time windows (p) → xr_customer_address_timewindow ─────
    @Column(name = "any_time_window")
    private Boolean anyTimeWindow;

    @Column(name = "from_time")
    private String fromTime;

    @Column(name = "to_time")
    private String toTime;

    // ── Route config (p) → xr_document_config ─────────────────
    @Column(name = "route_tag")
    private String routeTag;          // display_name_en

    @Column(name = "route_tag_fra")
    private String routeTagFra;       // display_name_fr

    @Column(name = "route_color")
    private String routeColor;        // color_code

    // ── Composite key class ────────────────────────────────────
    @Getter
    @Setter
    public static class StopEnrichmentId implements Serializable {
        private String docType;
        private String customerCode;
        private String addressCode;
    }
}
