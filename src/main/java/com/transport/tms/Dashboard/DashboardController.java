package com.transport.tms.Dashboard;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * GET /api/v1/dashboard?site=KCC01&startDate=2026-07-09&endDate=2026-07-09
 *
 * site      — optional, defaults to ALL sites
 * startDate — optional, defaults to today
 * endDate   — optional, defaults to today
 *
 * Presets (computed on frontend, sent as dates):
 *   Today      → startDate = endDate = today
 *   This Week  → startDate = Monday,  endDate = today
 *   This Month → startDate = 1st,     endDate = today
 *   Custom     → user picked range
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService service;

    @GetMapping
    public DashboardDTO getDashboard(
            @RequestParam(required = false) String site,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        LocalDate today = LocalDate.now();
        LocalDate from  = startDate != null ? startDate : today;
        LocalDate to    = endDate   != null ? endDate   : today;

        return service.getDashboard(site, from, to);
    }
}
