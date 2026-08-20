package com.transport.tms.Trip.Lock.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Replaces XX10CPLANCHD (X3 SQL Server) — one row per stop on a trip.
 * Written on Lock, read by the Route Management Detail screen's
 * "Transactions" panel — see VrController.getVrDetails().
 */
@Entity
@Table(name = "xr_vrdetails", schema = "tms")
@Getter
@Setter
public class VrDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "vr_detail_id")
    private UUID vrDetailId;

    @Column(name = "trip_code", nullable = false)
    private String tripCode;

    @Column(name = "doc_num")
    private String docNum;

    @Column(name = "line_num")
    private Integer lineNum;

    @Column(name = "seq")
    private Integer seq;

    @Column(name = "from_prev_distance")
    private Double fromPrevDistance;

    @Column(name = "from_prev_travel_time")
    private Double fromPrevTravelTime;

    @Column(name = "arrival_date")
    private LocalDateTime arrivalDate;

    @Column(name = "arrival_time")
    private String arrivalTime;

    @Column(name = "departure_date")
    private LocalDateTime departureDate;

    @Column(name = "departure_time")
    private String departureTime;

    @Column(name = "service_time")
    private String serviceTime;

    @Column(name = "waiting_time")
    private Double waitingTime;

    /** X3's list of values: 1=Delivery, 4=Pick Ticket (the only two this
     *  project ever writes). */
    @Column(name = "doc_type_code")
    private Integer docTypeCode;

    /** 1 = DROP, 2 = PICKUP */
    @Column(name = "pickup_drop")
    private Integer pickupDrop;

    /** LVS document status: Scheduled (default) -> In Progress (mobile
     *  app "Confirm Arrival") -> Delivered (mobile app "Departure").
     *  Only the default is set anywhere in this codebase yet — the two
     *  transitions depend on mobile-app services (Confirm Arrival,
     *  Departure) that haven't been built. */
    @Column(name = "doc_status")
    private String docStatus;

    @Column(name = "site")
    private String site;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
