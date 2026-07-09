package com.transport.tms.Dashboard;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class DashboardDTO {

    // ── KPI Cards ─────────────────────────────────────────────
    private KpiCard activeTrips;
    private KpiCard vehiclesOnRoad;
    private KpiCard driversOnDuty;
    private KpiCard deliveriesToday;

    // ── Fleet Status ──────────────────────────────────────────
    private FleetStatus fleetStatus;

    // ── Driver Hours ──────────────────────────────────────────
    private DriverHours driverHours;

    // ── Inner classes ─────────────────────────────────────────

    @Data @Builder
    public static class KpiCard {
        private int     value;
        private int     vsYesterday;     // delta vs yesterday (positive = up)
        private String  subtitle;        // e.g. "81% utilised", "94.1% on time"
    }

    @Data @Builder
    public static class FleetStatus {
        private int onRoad;
        private int idleDepot;
        private int maintenance;
        private int total;
        private int trailers;
        private int drivers;
        private double utilisationPct;   // onRoad / total * 100
    }

    @Data @Builder
    public static class DriverHours {
        private int    safe;             // < 8h
        private int    caution;          // 8-10h
        private int    alert;            // > 10h
        private int    maxHoursPerDay;   // from xr_driver (default 10)
        private String subtitle;         // "2 drivers on warning"
    }
}
