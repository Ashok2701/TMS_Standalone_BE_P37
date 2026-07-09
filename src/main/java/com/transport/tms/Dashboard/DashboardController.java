package com.transport.tms.Dashboard;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * GET /api/v1/dashboard?site=KCC01&date=2026-07-09
 *
 * site  — optional, defaults to ALL sites
 * date  — optional, defaults to today
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
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        return service.getDashboard(site, date);
    }
}
