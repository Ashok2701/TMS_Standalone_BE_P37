package com.transport.tms.Trip.Dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class TripRequestDTO {

    // ── Identity ─────────────────────────────────────────────
    private String    site;           // from vehicle.site / vehicle.fcy
    private LocalDate docDate;        // planning date

    // ── Driver ───────────────────────────────────────────────
    private String    driverId;       // driver.driverId
    private String    driverName;     // driver.driverName

    // ── Vehicle (from selected vehicle object) ───────────────
    private String    vehicleCode;    // vehicle.vehicleCode
    private String    depSite;        // vehicle.departureSite
    private String    arrSite;        // vehicle.arrivalSite

    // ── Counts ───────────────────────────────────────────────
    private Integer   drops;          // count of DROP stops
    private Integer   pickups;        // count of PICKUP stops
    private Integer   noOfPackages;   // sum of nbPack from all stops

    // ── Time (only startTime on confirm, rest filled on optimisation) ─
    private String    startTime;      // vehicle.startTime or "07:00"
    private String    endTime;        // empty on confirm — set on optimisation
    private String    travelTime;     // empty on confirm — set on optimisation
    private String    totalTime;      // empty on confirm — set on optimisation
    private String    serviceTime;    // empty on confirm — set on optimisation

    // ── Weight / Volume (from stops aggregate) ───────────────
    private String    totalWeight;    // sum of stop.netWeight
    private String    totalVolume;    // sum of stop.volume
    private String    capacity;       // vehicle.capacityWeight
    private String    uomCapacity;    // vehicle.weightUnit
    private String    uomVolume;      // vehicle.volumeUnit
    private String    uomTime;        // "HH:MM"
    private String    uomDistance;    // "mi" or "km"
    private Double    weightPct;      // totalWeight / vehicle.capacityWeight * 100
    private Double    volumePct;      // totalVolume / vehicle.capacityVolume * 100

    // ── Distance / Cost (empty on confirm — set on optimisation) ─
    private String    totalDistance;  // empty on confirm
    private String    totalCost;      // empty on confirm
    private String    distanceCost;   // empty on confirm
    private String    fixedCost;      // empty on confirm
    private String    serviceCost;    // empty on confirm
    private String    regularCost;    // empty on confirm
    private String    overtimeCost;   // empty on confirm

    // ── Control ──────────────────────────────────────────────
    private String    notes;
    private String    generatedBy;    // "PLANNER"
    private Integer   forceSeq;       // 0
    private String    vrSeq;

    // ── JSONB payloads ───────────────────────────────────────
    // stopObjects: array of ALL stops in sequence order at time of confirm
    // Each element = full stop object as it was when dragged/selected
    private List<Object> stopObjects;

    // vehicleObject: exact selected vehicle object snapshot at confirm time
    private Object    vehicleObject;

    // totalObject: empty on confirm, filled after optimisation
    private Object    totalObject;

    // ── Capacity fields ──────────────────────────────────────
    private String    totCapacity;
    private String    totVolumeCap;
    private String    docCapacity;
    private String    docVolume;
    private Double    perCapacity;
    private Double    perVolume;
    private Integer   docQty;
    private String    uomQty;
    private Integer   maxPalletCnt;

    // ── Audit ─────────────────────────────────────────────────
    private String    userCode;       // logged-in user code or "SYSTEM"
}
