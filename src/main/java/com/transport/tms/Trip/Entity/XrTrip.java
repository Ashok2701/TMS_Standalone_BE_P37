package com.transport.tms.Trip.Entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "xr_trip", schema = "tms")
@Getter @Setter
public class XrTrip {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trip_id") private Long tripId;

    @Column(name = "trip_code", nullable = false, unique = true, length = 60)
    private String tripCode;

    @Column(name = "site", nullable = false, length = 10) private String site;
    @Column(name = "doc_date", nullable = false)          private LocalDate docDate;

    @Column(name = "driver_id",   length = 30)  private String driverId;
    @Column(name = "driver_name", nullable = false, length = 100) private String driverName;
    @Column(name = "vehicle_code", nullable = false, length = 20) private String vehicleCode;

    @Column(name = "stops")   private Integer stops   = 0;
    @Column(name = "drops")   private Integer drops   = 0;
    @Column(name = "pickups") private Integer pickups = 0;
    @Column(name = "trips")   private Integer trips   = 1;
    @Column(name = "no_of_packages") private Integer noOfPackages;

    @Column(name = "dep_site", length = 20) private String depSite;
    @Column(name = "arr_site", length = 20) private String arrSite;

    @Column(name = "start_time",   length = 20) private String startTime;
    @Column(name = "end_time",     length = 20) private String endTime;
    @Column(name = "travel_time",  length = 20) private String travelTime;
    @Column(name = "total_time",   length = 20) private String totalTime;
    @Column(name = "service_time", length = 20) private String serviceTime;

    @Column(name = "total_weight",  length = 50) private String totalWeight;
    @Column(name = "total_volume",  length = 50) private String totalVolume;
    @Column(name = "capacity",      length = 50) private String capacity;
    @Column(name = "uom_capacity",  length = 10) private String uomCapacity;
    @Column(name = "uom_volume",    length = 10) private String uomVolume;
    @Column(name = "uom_time",      length = 10) private String uomTime;
    @Column(name = "uom_distance",  length = 20) private String uomDistance;
    @Column(name = "weight_pct")  private Double weightPct;
    @Column(name = "volume_pct")  private Double volumePct;

    @Column(name = "total_distance", length = 20) private String totalDistance;
    @Column(name = "fixed_cost",     length = 20) private String fixedCost;
    @Column(name = "distance_cost",  length = 20) private String distanceCost;
    @Column(name = "service_cost",   length = 20) private String serviceCost;
    @Column(name = "regular_cost",  length = 100) private String regularCost;
    @Column(name = "overtime_cost", length = 100) private String overtimeCost;
    @Column(name = "total_cost",     length = 20) private String totalCost;

    @Column(name = "opti_status", length = 20) private String optiStatus = "Open"; // Open | Optimised | Locked
    @Column(name = "lock_flag")   private Integer lockFlag  = 0;             // 1 = locked/sent to X3
    @Column(name = "force_seq")   private Integer forceSeq  = 0;
    @Column(name = "vr_seq",      length = 4)  private String vrSeq;
    @Column(name = "start_index") private Integer startIndex;
    @Column(name = "notes",       length = 200) private String notes;
    @Column(name = "generated_by",length = 50)  private String generatedBy;
    @Column(name = "heu_exec",    length = 7)   private String heuExec;
    @Column(name = "dat_exec")    private OffsetDateTime datExec;

    // JSONB columns — use @JdbcTypeCode(SqlTypes.JSON) so Hibernate
    // passes the value through the JSON type descriptor (handles PGobject binding)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "stop_objects",   columnDefinition = "jsonb")
    private String stopObjectsJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "vehicle_object", columnDefinition = "jsonb")
    private String vehicleObjectJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "total_object",   columnDefinition = "jsonb")
    private String totalObjectJson;

    @Column(name = "tot_capacity",  length = 100) private String totCapacity;
    @Column(name = "tot_volume_cap",length = 100) private String totVolumeCap;
    @Column(name = "doc_capacity",  length = 100) private String docCapacity;
    @Column(name = "doc_volume",    length = 100) private String docVolume;
    @Column(name = "per_capacity")  private Double perCapacity;
    @Column(name = "per_volume")    private Double perVolume;
    @Column(name = "doc_qty")       private Integer docQty;
    @Column(name = "uom_qty",    length = 5)  private String uomQty;
    @Column(name = "max_pallet_cnt") private Integer maxPalletCnt;
    @Column(name = "job_id",     length = 40) private String jobId;

    @Column(name = "alert_flag")    private Integer alertFlag   = 0;
    @Column(name = "warning_notes", columnDefinition = "TEXT") private String warningNotes;
    @Column(name = "appointment",   nullable = false) private Integer appointment  = 0;
    @Column(name = "freq_exist")    private Integer freqExist   = 0;
    @Column(name = "po_processed")  private Integer poProcessed = 0;

    @Column(name = "user_code", nullable = false, length = 10) private String userCode = "SYSTEM";
    @Column(name = "create_date", nullable = false, updatable = false) private OffsetDateTime createDate;
    @Column(name = "update_date", nullable = false)                    private OffsetDateTime updateDate;

    @PrePersist  protected void onCreate()  { createDate = updateDate = OffsetDateTime.now(); }
    @PreUpdate   protected void onUpdate()  { updateDate = OffsetDateTime.now(); }
}
