package com.transport.tms.RoutePlanner.Controller;

import com.transport.tms.RoutePlanner.Dto.RoutePlannerResponseDTO;
import com.transport.tms.RoutePlanner.Dto.RoutePlannerSiteDTO;
import com.transport.tms.RoutePlanner.Service.RoutePlannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("${transport.path}/route-planner")
@RequiredArgsConstructor
public class RoutePlannerController {

    private final RoutePlannerService service;

    // ─────────────────────────────────────────────────────────
    // GET /api/v1/route-planner/sites
    // Returns all TMS-enabled sites (tms_flag = 2) for dropdown
    // ─────────────────────────────────────────────────────────
    @GetMapping("/sites")
    public ResponseEntity<List<RoutePlannerSiteDTO>> getTmsSites() {

        return ResponseEntity.ok(service.getAllTmsSites());
    }

    // ─────────────────────────────────────────────────────────
    // GET /api/v1/route-planner/load?siteCode=LEWISB&planDate=2025-06-16
    //
    // Returns:
    //   site        — site detail from Postgres (xr_site)
    //   vehicles    — all active vehicles from Postgres (xr_vehicle)
    //   drivers     — all active drivers from Postgres (xr_driver)
    //   drops       — delivery orders from X3 SQL Server (site + date)
    //   pickups     — pickup/receipt orders from X3 SQL Server (site + date)
    // ─────────────────────────────────────────────────────────
    @GetMapping("/load")
    public ResponseEntity<RoutePlannerResponseDTO> loadPlannerData(

            @RequestParam String siteCode,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate planDate) {

        return ResponseEntity.ok(
                service.loadPlannerData(siteCode, planDate));
    }
}
