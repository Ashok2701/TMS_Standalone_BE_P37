package com.transport.tms.Dashboard;

import com.transport.tms.Fleet.Entity.Driver;
import com.transport.tms.Fleet.Entity.Vehicle;
import com.transport.tms.Fleet.Repository.DriverRepository;
import com.transport.tms.Fleet.Repository.VehicleRepository;
import com.transport.tms.Trip.Entity.XrTrip;
import com.transport.tms.Trip.Repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TripRepository    tripRepo;
    private final VehicleRepository vehicleRepo;
    private final DriverRepository  driverRepo;

    // ── Main entry point ──────────────────────────────────────
    public DashboardDTO getDashboard(String site, LocalDate date) {

        LocalDate today     = date != null ? date : LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        // Trips for today and yesterday
        List<XrTrip> todayTrips = getTrips(site, today);
        List<XrTrip> yestTrips  = getTrips(site, yesterday);

        // Active vehicles and drivers
        List<Vehicle> allVehicles = getVehicles(site);
        List<Driver>  allDrivers  = driverRepo.findAll().stream()
                .filter(d -> Boolean.TRUE.equals(d.getActive()))
                .toList();

        return DashboardDTO.builder()
                .activeTrips(    buildActiveTrips(todayTrips, yestTrips))
                .vehiclesOnRoad( buildVehiclesOnRoad(todayTrips, allVehicles, yestTrips))
                .driversOnDuty(  buildDriversOnDuty(todayTrips, yestTrips))
                .deliveriesToday(buildDeliveries(todayTrips, yestTrips))
                .fleetStatus(    buildFleetStatus(todayTrips, allVehicles, allDrivers))
                .driverHours(    buildDriverHours(todayTrips, allDrivers))
                .build();
    }

    // ── KPI: Active Trips ─────────────────────────────────────
    // All trips for the date regardless of status
    private DashboardDTO.KpiCard buildActiveTrips(List<XrTrip> today, List<XrTrip> yesterday) {
        int val  = today.size();
        int yest = yesterday.size();
        return DashboardDTO.KpiCard.builder()
                .value(val)
                .vsYesterday(val - yest)
                .subtitle((val - yest) >= 0
                        ? "+" + (val - yest) + " vs yesterday"
                        : (val - yest) + " vs yesterday")
                .build();
    }

    // ── KPI: Vehicles on Road ─────────────────────────────────
    // Vehicles with a LOCKED trip today (sent to X3 = on road)
    private DashboardDTO.KpiCard buildVehiclesOnRoad(
            List<XrTrip> today, List<Vehicle> allVehicles, List<XrTrip> yesterday) {

        long onRoad = today.stream()
                .filter(t -> t.getLockFlag() != null && t.getLockFlag() == 1)
                .map(XrTrip::getVehicleCode)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        long onRoadYest = yesterday.stream()
                .filter(t -> t.getLockFlag() != null && t.getLockFlag() == 1)
                .map(XrTrip::getVehicleCode)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        int total = allVehicles.size();
        double pct = total > 0 ? Math.round((double) onRoad / total * 1000.0) / 10.0 : 0;

        return DashboardDTO.KpiCard.builder()
                .value((int) onRoad)
                .vsYesterday((int)(onRoad - onRoadYest))
                .subtitle(pct + "% utilised")
                .build();
    }

    // ── KPI: Drivers on Duty ─────────────────────────────────
    // Distinct drivers with a locked trip today
    private DashboardDTO.KpiCard buildDriversOnDuty(
            List<XrTrip> today, List<XrTrip> yesterday) {

        long onDuty = today.stream()
                .filter(t -> t.getLockFlag() != null && t.getLockFlag() == 1)
                .map(XrTrip::getDriverId)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        long onDutyYest = yesterday.stream()
                .filter(t -> t.getLockFlag() != null && t.getLockFlag() == 1)
                .map(XrTrip::getDriverId)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        // Drivers approaching hour limit
        long approaching = countApproachingHourLimit(today);

        return DashboardDTO.KpiCard.builder()
                .value((int) onDuty)
                .vsYesterday((int)(onDuty - onDutyYest))
                .subtitle(approaching > 0 ? approaching + " approaching hour limit" : "")
                .build();
    }

    // ── KPI: Deliveries Today ─────────────────────────────────
    // Sum of drops + pickups across all trips today
    private DashboardDTO.KpiCard buildDeliveries(
            List<XrTrip> today, List<XrTrip> yesterday) {

        int val = today.stream()
                .mapToInt(t -> (t.getDrops() != null ? t.getDrops() : 0)
                             + (t.getPickups() != null ? t.getPickups() : 0))
                .sum();

        int yest = yesterday.stream()
                .mapToInt(t -> (t.getDrops() != null ? t.getDrops() : 0)
                             + (t.getPickups() != null ? t.getPickups() : 0))
                .sum();

        return DashboardDTO.KpiCard.builder()
                .value(val)
                .vsYesterday(val - yest)
                .subtitle("94.1% on time")   // static for now
                .build();
    }

    // ── Fleet Status panel ────────────────────────────────────
    private DashboardDTO.FleetStatus buildFleetStatus(
            List<XrTrip> todayTrips, List<Vehicle> allVehicles, List<Driver> allDrivers) {

        // Vehicles in a locked trip today = On Road
        Set<String> onRoadCodes = todayTrips.stream()
                .filter(t -> t.getLockFlag() != null && t.getLockFlag() == 1)
                .map(XrTrip::getVehicleCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        int total       = allVehicles.size();
        int onRoad      = (int) allVehicles.stream()
                            .filter(v -> onRoadCodes.contains(v.getVehicleCode()))
                            .count();
        // vehicle_status: 1=Available, 2=Idle/Depot, 3=Maintenance
        int maintenance = (int) allVehicles.stream()
                            .filter(v -> v.getVehicleStatus() != null && v.getVehicleStatus() == 3)
                            .count();
        int idle        = total - onRoad - maintenance;
        if (idle < 0) idle = 0;

        // Trailers: vehicles with a trailer_number set
        int trailers = (int) allVehicles.stream()
                            .filter(v -> v.getTrailerNumber() != null
                                    && !v.getTrailerNumber().isBlank())
                            .count();

        // Active drivers
        int driverCount = (int) allDrivers.stream()
                            .filter(d -> d.getDriverStatus() != null
                                    && d.getDriverStatus() == 1)
                            .count();

        double utilisationPct = total > 0
                ? Math.round((double) onRoad / total * 1000.0) / 10.0
                : 0.0;

        return DashboardDTO.FleetStatus.builder()
                .onRoad(onRoad)
                .idleDepot(idle)
                .maintenance(maintenance)
                .total(total)
                .trailers(trailers)
                .drivers(driverCount)
                .utilisationPct(utilisationPct)
                .build();
    }

    // ── Driver Hours Today ────────────────────────────────────
    // Hours per driver = sum of (endTime - startTime) across all trips today
    private DashboardDTO.DriverHours buildDriverHours(
            List<XrTrip> todayTrips, List<Driver> allDrivers) {

        // Group trips by driver, calculate total hours per driver
        Map<String, Double> driverHoursMap = new HashMap<>();

        for (XrTrip trip : todayTrips) {
            if (trip.getDriverId() == null) continue;
            double hours = calcTripHours(trip.getStartTime(), trip.getEndTime());
            driverHoursMap.merge(trip.getDriverId(), hours, Double::sum);
        }

        int safe     = 0;
        int caution  = 0;
        int alert    = 0;
        int maxHours = 10; // default

        for (Map.Entry<String, Double> entry : driverHoursMap.entrySet()) {
            double h = entry.getValue();
            if      (h < 8)  safe++;
            else if (h <= 10) caution++;
            else              alert++;
        }

        int warning = caution + alert;

        return DashboardDTO.DriverHours.builder()
                .safe(safe)
                .caution(caution)
                .alert(alert)
                .maxHoursPerDay(maxHours)
                .subtitle("Max limit: " + maxHours + "h/day"
                        + (warning > 0 ? " · " + warning + " drivers on warning" : ""))
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────

    private List<XrTrip> getTrips(String site, LocalDate date) {
        if (site == null || site.isBlank() || "ALL".equalsIgnoreCase(site)) {
            return tripRepo.findAll().stream()
                    .filter(t -> date.equals(t.getDocDate()))
                    .toList();
        }
        return tripRepo.findBySiteAndDocDateOrderByCreateDateAsc(site, date);
    }

    private List<Vehicle> getVehicles(String site) {
        return vehicleRepo.findAll().stream()
                .filter(v -> Boolean.TRUE.equals(v.getActive()))
                .filter(v -> site == null || site.isBlank()
                          || "ALL".equalsIgnoreCase(site)
                          || site.equals(v.getSite()))
                .toList();
    }

    private long countApproachingHourLimit(List<XrTrip> trips) {
        Map<String, Double> hours = new HashMap<>();
        for (XrTrip t : trips) {
            if (t.getDriverId() == null) continue;
            hours.merge(t.getDriverId(), calcTripHours(t.getStartTime(), t.getEndTime()), Double::sum);
        }
        // Approaching = between 8 and 10 hours
        return hours.values().stream().filter(h -> h >= 8 && h <= 10).count();
    }

    /** Parse HH:MM time string → hours as double. Returns 0 if unparseable. */
    private double calcTripHours(String startTime, String endTime) {
        try {
            if (startTime == null || endTime == null
                    || startTime.isBlank() || endTime.isBlank()) return 0;
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime start = LocalTime.parse(startTime.trim().substring(0, 5), fmt);
            LocalTime end   = LocalTime.parse(endTime.trim().substring(0, 5), fmt);
            int mins = end.toSecondOfDay() / 60 - start.toSecondOfDay() / 60;
            if (mins < 0) mins += 24 * 60; // overnight trip
            return mins / 60.0;
        } catch (Exception e) {
            return 0;
        }
    }
}
