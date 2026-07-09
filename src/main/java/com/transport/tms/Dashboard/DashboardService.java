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
    public DashboardDTO getDashboard(String site, LocalDate startDate, LocalDate endDate) {

        LocalDate today = LocalDate.now();
        LocalDate from  = startDate != null ? startDate : today;
        LocalDate to    = endDate   != null ? endDate   : today;

        // "yesterday" relative to startDate for vs comparison
        LocalDate prevFrom = from.minusDays(to.toEpochDay() - from.toEpochDay() + 1);
        LocalDate prevTo   = from.minusDays(1);

        List<XrTrip> rangeTrips = getTrips(site, from, to);
        List<XrTrip> prevTrips  = getTrips(site, prevFrom, prevTo);

        List<Vehicle> allVehicles = getVehicles(site);
        List<Driver>  allDrivers  = driverRepo.findAll().stream()
                .filter(d -> Boolean.TRUE.equals(d.getActive()))
                .toList();

        return DashboardDTO.builder()
                .activeTrips(    buildActiveTrips(rangeTrips, prevTrips))
                .vehiclesOnRoad( buildVehiclesOnRoad(rangeTrips, allVehicles, prevTrips))
                .driversOnDuty(  buildDriversOnDuty(rangeTrips, prevTrips))
                .deliveriesToday(buildDeliveries(rangeTrips, prevTrips))
                .fleetStatus(    buildFleetStatus(rangeTrips, allVehicles, allDrivers))
                .driverHours(    buildDriverHours(rangeTrips, allDrivers))
                .build();
    }

    // ── KPI: Active Trips ─────────────────────────────────────
    private DashboardDTO.KpiCard buildActiveTrips(List<XrTrip> range, List<XrTrip> prev) {
        int val  = range.size();
        int yest = prev.size();
        int delta = val - yest;
        return DashboardDTO.KpiCard.builder()
                .value(val)
                .vsYesterday(delta)
                .subtitle((delta >= 0 ? "+" : "") + delta + " vs previous period")
                .build();
    }

    // ── KPI: Vehicles on Road ─────────────────────────────────
    private DashboardDTO.KpiCard buildVehiclesOnRoad(
            List<XrTrip> range, List<Vehicle> allVehicles, List<XrTrip> prev) {

        long onRoad = range.stream()
                .filter(t -> t.getLockFlag() != null && t.getLockFlag() == 1)
                .map(XrTrip::getVehicleCode)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        long onRoadPrev = prev.stream()
                .filter(t -> t.getLockFlag() != null && t.getLockFlag() == 1)
                .map(XrTrip::getVehicleCode)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        int total = allVehicles.size();
        double pct = total > 0
                ? Math.round((double) onRoad / total * 1000.0) / 10.0
                : 0.0;

        return DashboardDTO.KpiCard.builder()
                .value((int) onRoad)
                .vsYesterday((int)(onRoad - onRoadPrev))
                .subtitle(pct + "% utilised")
                .build();
    }

    // ── KPI: Drivers on Duty ─────────────────────────────────
    private DashboardDTO.KpiCard buildDriversOnDuty(
            List<XrTrip> range, List<XrTrip> prev) {

        long onDuty = range.stream()
                .filter(t -> t.getLockFlag() != null && t.getLockFlag() == 1)
                .map(XrTrip::getDriverId)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        long onDutyPrev = prev.stream()
                .filter(t -> t.getLockFlag() != null && t.getLockFlag() == 1)
                .map(XrTrip::getDriverId)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        long approaching = countApproachingHourLimit(range);

        return DashboardDTO.KpiCard.builder()
                .value((int) onDuty)
                .vsYesterday((int)(onDuty - onDutyPrev))
                .subtitle(approaching > 0 ? approaching + " approaching hour limit" : "")
                .build();
    }

    // ── KPI: Deliveries ───────────────────────────────────────
    private DashboardDTO.KpiCard buildDeliveries(
            List<XrTrip> range, List<XrTrip> prev) {

        int val = range.stream()
                .mapToInt(t -> (t.getDrops()   != null ? t.getDrops()   : 0)
                             + (t.getPickups() != null ? t.getPickups() : 0))
                .sum();

        int prevVal = prev.stream()
                .mapToInt(t -> (t.getDrops()   != null ? t.getDrops()   : 0)
                             + (t.getPickups() != null ? t.getPickups() : 0))
                .sum();

        return DashboardDTO.KpiCard.builder()
                .value(val)
                .vsYesterday(val - prevVal)
                .subtitle("94.1% on time")
                .build();
    }

    // ── Fleet Status ──────────────────────────────────────────
    private DashboardDTO.FleetStatus buildFleetStatus(
            List<XrTrip> rangeTrips, List<Vehicle> allVehicles, List<Driver> allDrivers) {

        Set<String> onRoadCodes = rangeTrips.stream()
                .filter(t -> t.getLockFlag() != null && t.getLockFlag() == 1)
                .map(XrTrip::getVehicleCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        int total       = allVehicles.size();
        int onRoad      = (int) allVehicles.stream()
                            .filter(v -> onRoadCodes.contains(v.getVehicleCode()))
                            .count();
        int maintenance = (int) allVehicles.stream()
                            .filter(v -> v.getVehicleStatus() != null && v.getVehicleStatus() == 3)
                            .count();
        int idle        = Math.max(0, total - onRoad - maintenance);

        int trailers    = (int) allVehicles.stream()
                            .filter(v -> v.getTrailerNumber() != null
                                    && !v.getTrailerNumber().isBlank())
                            .count();

        int driverCount = (int) allDrivers.stream()
                            .filter(d -> d.getDriverStatus() != null && d.getDriverStatus() == 1)
                            .count();

        double utilisationPct = total > 0
                ? Math.round((double) onRoad / total * 1000.0) / 10.0
                : 0.0;

        return DashboardDTO.FleetStatus.builder()
                .onRoad(onRoad).idleDepot(idle).maintenance(maintenance)
                .total(total).trailers(trailers).drivers(driverCount)
                .utilisationPct(utilisationPct)
                .build();
    }

    // ── Driver Hours ──────────────────────────────────────────
    private DashboardDTO.DriverHours buildDriverHours(
            List<XrTrip> rangeTrips, List<Driver> allDrivers) {

        // Group trips by driver → sum hours
        Map<String, Double> hoursMap = new HashMap<>();
        for (XrTrip trip : rangeTrips) {
            if (trip.getDriverId() == null) continue;
            double h = calcTripHours(trip.getStartTime(), trip.getEndTime());
            hoursMap.merge(trip.getDriverId(), h, Double::sum);
        }

        int safe = 0, caution = 0, alert = 0;
        for (double h : hoursMap.values()) {
            if      (h < 8)  safe++;
            else if (h <= 10) caution++;
            else              alert++;
        }

        int warning = caution + alert;
        return DashboardDTO.DriverHours.builder()
                .safe(safe).caution(caution).alert(alert)
                .maxHoursPerDay(10)
                .subtitle("Max limit: 10h/day"
                        + (warning > 0 ? " · " + warning + " drivers on warning" : ""))
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────

    /** Fetch trips for site within date range (inclusive) */
    private List<XrTrip> getTrips(String site, LocalDate from, LocalDate to) {
        List<XrTrip> all = tripRepo.findAll();
        return all.stream()
                .filter(t -> t.getDocDate() != null
                        && !t.getDocDate().isBefore(from)
                        && !t.getDocDate().isAfter(to))
                .filter(t -> site == null || site.isBlank()
                        || "ALL".equalsIgnoreCase(site)
                        || site.equals(t.getSite()))
                .toList();
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
        return hours.values().stream().filter(h -> h >= 8 && h <= 10).count();
    }

    private double calcTripHours(String startTime, String endTime) {
        try {
            if (startTime == null || endTime == null
                    || startTime.isBlank() || endTime.isBlank()) return 0;
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime start = LocalTime.parse(startTime.trim().substring(0, 5), fmt);
            LocalTime end   = LocalTime.parse(endTime.trim().substring(0, 5), fmt);
            int mins = end.toSecondOfDay() / 60 - start.toSecondOfDay() / 60;
            if (mins < 0) mins += 24 * 60;
            return mins / 60.0;
        } catch (Exception e) {
            return 0;
        }
    }
}
