package com.transport.tms.Trip.Dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class TripResponseDTO {
    private Long     tripId;
    private String   tripCode;
    private String   site;
    private LocalDate docDate;

    private String   driverId;
    private String   driverName;
    private String   vehicleCode;

    private Integer  stops;
    private Integer  drops;
    private Integer  pickups;
    private Integer  noOfPackages;

    private String   depSite;
    private String   arrSite;

    private String   startTime;
    private String   endTime;
    private String   travelTime;
    private String   totalTime;
    private String   serviceTime;

    private String   totalWeight;
    private String   totalVolume;
    private String   capacity;
    private String   uomCapacity;
    private String   uomVolume;
    private Double   weightPct;
    private Double   volumePct;

    private String   totalDistance;
    private String   totalCost;
    private String   distanceCost;
    private String   fixedCost;
    private String   serviceCost;

    private String   optiStatus;
    private Integer  lockFlag;
    private Integer  forceSeq;
    private String   vrSeq;
    private String   notes;
    private String   generatedBy;

    private Object   stopObjects;
    private Object   vehicleObject;
    private Object   totalObject;

    private String   uomTime;
    private String   uomDistance;

    private Double   perCapacity;
    private Double   perVolume;
    private Integer  docQty;

    private Integer  alertFlag;
    private String   warningNotes;

    private String   userCode;
    private OffsetDateTime createDate;
    private OffsetDateTime updateDate;
}
