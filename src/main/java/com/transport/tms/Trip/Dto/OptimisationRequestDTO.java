package com.transport.tms.Trip.Dto;

import lombok.Data;
import java.util.List;

/**
 * Payload sent to PATCH /api/v1/trips/{id}/optimise
 *
 * Contains the optimisation settings AND the per-stop results
 * (arrival/departure times, distance and travel time from previous stop).
 * The service merges stopResults back into the trip's stopObjects JSONB.
 */
@Data
public class OptimisationRequestDTO {

    // ── Optimisation settings ─────────────────────────────────
    private String  orderMode;      // "fixed" | "auto"
    private String  startTime;      // departure time from depot e.g. "07:30"
    private String  endTime;        // return time to depot  e.g. "18:30"

    // ── Trip-level totals (filled by optimisation engine) ─────
    private String  travelTime;     // total travel time   e.g. "02:15"
    private String  totalTime;      // total trip time     e.g. "03:30"
    private String  serviceTime;    // total service time  e.g. "01:15"
    private String  totalDistance;  // total distance      e.g. "128"
    private String  uomDistance;    // "mi" | "km"
    private String  uomTime;        // "HH:MM"

    // ── Cost fields (from optimisation engine) ────────────────
    private String  totalCost;
    private String  distanceCost;
    private String  fixedCost;
    private String  serviceCost;
    private String  regularCost;
    private String  overtimeCost;

    // ── Per-stop results — merged into stopObjects JSONB ──────
    // List must be in the same sequence order as stopObjects
    private Object totalObject;   // totals snapshot — stored in total_object JSONB

    private List<StopOptimisationResult> stopResults;

    // ── Inner class: result for one stop ─────────────────────
    @Data
    public static class StopOptimisationResult {

        private Integer seq;               // sequence position (1-based)
        private String  docNum;            // matches stop txn/docNum to identify the stop

        // Arrival / Departure at this stop
        private String  arrivalDate;       // "2026-06-23"
        private String  arrivalTime;       // "08:45"
        private String  departureDate;     // "2026-06-23"
        private String  departureTime;     // "09:15"

        // From previous stop (or depot for stop 1)
        private String  fromPrevDistance;  // e.g. "18"   (uomDistance applies)
        private String  fromPrevTravelTime;// e.g. "00:36" (HH:MM)

        // At-stop metrics
        private String  serviceTime;       // time spent at this stop e.g. "00:30"
        private String  waitingTime;       // waiting time if arrived early
    }
}
