package com.transport.tms.RoutePlanner.Repository;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Reads from tms.vw_rp_stop_enrich (UNION ALL of dlv + pick enrichment views).
 * Keyed by (customer_code + address_code + doc_type).
 * Provides all (p) fields: lat/lon, service time, waiting time,
 * time windows, route tag/color from xr_document_config.
 */
@Repository
public interface StopEnrichmentRepository
        extends JpaRepository<StopEnrichmentRepository.StopEnrichment,
                              StopEnrichmentRepository.StopEnrichmentId> {

    /**
     * Batch fetch enrichment for a list of customer codes + doc type.
     * Called once per stop list (drops or pickups) after X3 fetch.
     * doc_type = 'DLV' for deliveries, 'PICK' for pick tickets.
     */
    @Query("""
        SELECT e FROM StopEnrichment e
        WHERE  e.id.customerCode IN :customerCodes
          AND  e.id.docType       = :docType
    """)
    List<StopEnrichment> findByCustomerCodesAndDocType(
            @Param("customerCodes") List<String> customerCodes,
            @Param("docType")       String docType);

    // ── Entity mapped to tms.vw_rp_stop_enrich ────────────────
    @Entity
    @Table(name = "vw_rp_stop_enrich", schema = "tms")
    @IdClass(StopEnrichmentId.class)
    @Getter
    @Setter
    class StopEnrichment {

        // ── Composite PK ──────────────────────────────────────
        @Id
        @Column(name = "doc_type")
        private String docType;           // 'DLV' or 'PICK'

        @Id
        @Column(name = "customer_code")
        private String customerCode;

        @Id
        @Column(name = "address_code")
        private String addressCode;

        // ── Geo (p) → xr_customer ─────────────────────────────
        @Column(name = "latitude")
        private BigDecimal latitude;

        @Column(name = "longitude")
        private BigDecimal longitude;

        // ── Service / waiting time (p) → xr_customer ──────────
        @Column(name = "service_time")
        private String serviceTime;

        @Column(name = "waiting_time")
        private String waitingTime;

        // ── Time windows (p) → xr_customer_address_timewindow ─
        @Column(name = "any_time_window")
        private Boolean anyTimeWindow;

        @Column(name = "from_time")
        private String fromTime;

        @Column(name = "to_time")
        private String toTime;

        // ── Route config (p) → xr_document_config ─────────────
        @Column(name = "route_tag")
        private String routeTag;          // display_name_en

        @Column(name = "route_tag_fra")
        private String routeTagFra;       // display_name_fr

        @Column(name = "route_color")
        private String routeColor;        // color_code
    }

    // ── Composite key class ────────────────────────────────────
    @Getter
    @Setter
    class StopEnrichmentId implements Serializable {
        private String docType;
        private String customerCode;
        private String addressCode;
    }
}
