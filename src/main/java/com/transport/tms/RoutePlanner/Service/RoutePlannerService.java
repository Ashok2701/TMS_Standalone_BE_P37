package com.transport.tms.RoutePlanner.Service;

import com.transport.tms.RoutePlanner.Dto.RoutePlannerResponseDTO;
import com.transport.tms.RoutePlanner.Dto.RoutePlannerSiteDTO;

import java.time.LocalDate;
import java.util.List;

public interface RoutePlannerService {

    // Get all TMS-enabled sites (tms_flag = 2)
    List<RoutePlannerSiteDTO> getAllTmsSites();

    // Main route planner data load: site + date → vehicles, drivers, drops, pickups
    RoutePlannerResponseDTO loadPlannerData(
            String siteCode,
            LocalDate planDate);
}
