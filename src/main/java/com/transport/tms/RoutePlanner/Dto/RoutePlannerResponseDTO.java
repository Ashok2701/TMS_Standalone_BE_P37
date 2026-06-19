package com.transport.tms.RoutePlanner.Dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class RoutePlannerResponseDTO {

    // Echo back the request context
    private String siteCode;

    private String siteName;

    private LocalDate planDate;

    // Site detail (from Postgres xr_site where tms_flag = 2)
    private RoutePlannerSiteDTO site;

    // Active vehicles (from Postgres xr_vehicle where active = true)
    private List<RoutePlannerVehicleDTO> vehicles;

    // Active drivers (from Postgres xr_driver where active = true)
    private List<RoutePlannerDriverDTO> drivers;

    // Drops (from X3 SQL Server — site + date filtered)
    private List<RoutePlannerStopDTO> drops;

    // Pickups (from X3 SQL Server — site + date filtered)
    private List<RoutePlannerStopDTO> pickups;

    // Summary counts
    private Integer vehicleCount;

    private Integer driverCount;

    private Integer dropCount;

    private Integer pickupCount;
}
