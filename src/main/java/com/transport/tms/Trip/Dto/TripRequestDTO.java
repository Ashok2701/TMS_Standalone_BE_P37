package com.transport.tms.Trip.Dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class TripRequestDTO {
    private String site;
    private LocalDate docDate;

    // Driver
    private String driverId;
    private String driverName;

    // Vehicle
    private String vehicleCode;
    private String depSite;
    private String arrSite;

    // Counts
    private Integer drops;
    private Integer pickups;
    private Integer noOfPackages;

    // Time
    private String startTime;
    private String endTime;
    private String travelTime;
    private String totalTime;
    private String serviceTime;

    // Weight / Volume
    private String totalWeight;
    private String totalVolume;
    private String capacity;
    private String uomCapacity;
    private String uomVolume;
    private String uomTime;
    private String uomDistance;
    private Double weightPct;
    private Double volumePct;

    // Distance / Cost
    private String totalDistance;
    private String totalCost;
    private String distanceCost;
    private String fixedCost;
    private String serviceCost;
    private String regularCost;
    private String overtimeCost;

    // Control
    private String notes;
    private String generatedBy;
    private Integer forceSeq;
    private String vrSeq;

    // JSONB payloads
    private List<Object> stopObjects;    // drops + pickups array
    private Object vehicleObject;        // vehicle snapshot
    private Object totalObject;          // totals snapshot

    // Capacity fields
    private String totCapacity;
    private String totVolumeCap;
    private String docCapacity;
    private String docVolume;
    private Double perCapacity;
    private Double perVolume;
    private Integer docQty;
    private String uomQty;
    private Integer maxPalletCnt;

    // Audit
    private String userCode;
}
