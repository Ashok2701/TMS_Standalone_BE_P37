package com.transport.tms.Sync.Site.Entity;


import jakarta.persistence.*;

import lombok.Getter;

import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;




@Entity

@Table(name="xr_site",  schema="tms")

@Getter
@Setter

public class XRSite {
    @Id
    @Column(name="site_code")
    private String siteCode;


    @Column(name="site_name")
    private String siteName;


    @Column(name="short_name")
    private String shortName;


    @Column(name="country_code")
    private String countryCode;



    @Column(name="address_code")
    private String addressCode;


    @Column(name="address_description")
    private String addressDescription;


    @Column(name="address_line1")
    private String addressLine1;


    @Column(name="address_line2")
    private String addressLine2;


    @Column(name="address_line3")
    private String addressLine3;


    @Column(name="city")
    private String city;


    @Column(name="postal_code")
    private String postalCode;


    @Column(name="state_code")
    private String stateCode;


    @Column(name="country_name")
    private String countryName;


    @Column(name="synced_at")
    private LocalDateTime syncedAt;



    // ======================
    // TMS FIELDS
    // ======================


    @Column(name="latitude")
    private BigDecimal latitude;


    @Column(name="longitude")
    private BigDecimal longitude;


    @Column(name="working_start_time")
    private String workingStartTime;


    @Column(name="working_end_time")
    private String workingEndTime;


    @Column(name="loading_dock_count")
    private Integer loadingDockCount;


    @Column(name="max_vehicle_capacity")
    private Integer maxVehicleCapacity;


    @Column(name="tms_flag")
    private Boolean tmsFlag;


    @Column(name="remarks")
    private String remarks;



    // ======================
    // AUDIT
    // ======================


    @Column(name="created_by")
    private String createdBy;


    @Column(name="created_at")
    private LocalDateTime createdAt;


    @Column(name="updated_by")
    private String updatedBy;


    @Column(name="updated_at")
    private LocalDateTime updatedAt;


}