package com.transport.tms.RoutePlanner.Impl;

import com.transport.tms.Fleet.Entity.Driver;
import com.transport.tms.Fleet.Entity.Vehicle;
import com.transport.tms.GlobalException.ApplicationException;
import com.transport.tms.RoutePlanner.Dto.*;
import com.transport.tms.RoutePlanner.Repository.RoutePlannerDriverRepository;
import com.transport.tms.RoutePlanner.Repository.RoutePlannerSiteRepository;
import com.transport.tms.RoutePlanner.Repository.RoutePlannerVehicleRepository;
import com.transport.tms.RoutePlanner.Repository.StopEnrichmentRepository;
import com.transport.tms.RoutePlanner.Repository.StopProductRepository;
import com.transport.tms.RoutePlanner.Dto.StopProductDTO;
import com.transport.tms.RoutePlanner.Repository.StopEnrichment;

import com.transport.tms.RoutePlanner.Repository.X3RoutePlannerRepository;
import com.transport.tms.RoutePlanner.Service.RoutePlannerService;
import com.transport.tms.Sync.Site.Entity.XRSite;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoutePlannerServiceImpl implements RoutePlannerService {

    private final RoutePlannerSiteRepository    siteRepository;
    private final RoutePlannerVehicleRepository vehicleRepository;
    private final RoutePlannerDriverRepository  driverRepository;
    private final X3RoutePlannerRepository      x3Repository;
    private final StopEnrichmentRepository      enrichmentRepository;
    private final StopProductRepository          productRepository;

    // ─────────────────────────────────────────────────────────
    // GET ALL TMS SITES
    // ─────────────────────────────────────────────────────────
    @Override
    public List<RoutePlannerSiteDTO> getAllTmsSites() {
        log.info("RoutePlanner: fetching all TMS sites");
        return siteRepository.findAllTmsSites()
                .stream()
                .map(this::mapSite)
                .toList();
    }

    // ─────────────────────────────────────────────────────────
    // MAIN PLANNER DATA LOAD
    //
    // Flow:
    //  1. Validate site (Postgres)
    //  2. Vehicles (Postgres)
    //  3. Drivers  (Postgres)
    //  4. Deliveries (SQL Server XTMSDLVY_TMS — x fields only)
    //  5. Pick tickets (SQL Server XTMSPICK_TMS — x fields only)
    //  6. Enrich deliveries   (Postgres vw_rp_stop_enrich, doc_type='DLV')
    //     → in-memory join by BPCODE + ADRESCODE
    //     → fills lat/lon, service_time, waiting_time,
    //              route_tag, route_color, time windows
    //  7. Enrich pick tickets (same, doc_type='PICK')
    //  8. Business rule: "Drops" = deliveries + pick tickets
    //     (both are things dropped off at the customer location).
    //     "Pickups" = collecting from a customer/supplier location —
    //     not currently sourced from X3, so this bucket is empty
    //     until that data source is identified/added.
    //  9. Build combined RoutePlannerResponseDTO
    // ─────────────────────────────────────────────────────────
    @Override
    public RoutePlannerResponseDTO loadPlannerData(String siteCode, LocalDate planDate) {

        log.info("RoutePlanner: loading data — site={} date={}", siteCode, planDate);

        // ── 1. Validate site ──────────────────────────────────
        XRSite site = siteRepository.findTmsSiteBySiteCode(siteCode)
                .orElseThrow(() -> new ApplicationException(
                        404, "Site not found or not TMS-enabled: " + siteCode));

        // ── 2. Vehicles from Postgres ─────────────────────────
        List<Vehicle> vehicles = vehicleRepository.findAllActiveVehicles();
        log.info("RoutePlanner: {} vehicles", vehicles.size());

        // ── 3. Drivers from Postgres ──────────────────────────
        List<Driver> drivers = driverRepository.findAllActiveDrivers();
        log.info("RoutePlanner: {} drivers", drivers.size());

        // ── 4. Deliveries from SQL Server (XTMSDLVY_TMS) ──────
        List<RoutePlannerStopDTO> deliveries = fetchX3Stops(
                () -> x3Repository.findDropsByDateAndSite(siteCode, planDate),
                "deliveries");

        // ── 5. Pick tickets from SQL Server (XTMSPICK_TMS) ────
        // NOTE: these are picking/fulfillment documents for outbound
        // deliveries — they are dropped at the customer, same as
        // deliveries. They are NOT customer/supplier pickups.
        List<RoutePlannerStopDTO> pickTickets = fetchX3Stops(
                () -> x3Repository.findPickupsByDateAndSite(siteCode, planDate),
                "pick-tickets");

        // ── 6. Enrich deliveries with Postgres p-fields ───────
        enrichStops(deliveries, "DLV");

        // ── 7. Enrich pick tickets with Postgres p-fields ─────
        enrichStops(pickTickets, "PICK");

        // ── 8. Load product lines per underlying source table ─
        // (this is keyed to the X3 source table, not the business
        // bucket — deliveries and pick tickets have different
        // product-line tables regardless of both being "drops")
        loadProducts(deliveries,   "DROP");
        loadProducts(pickTickets, "PICKUP");

        // ── 9. Business bucket: Drops = deliveries + pick tickets ──
        List<RoutePlannerStopDTO> drops = new java.util.ArrayList<>(
                deliveries.size() + pickTickets.size());
        drops.addAll(deliveries);
        drops.addAll(pickTickets);

        // True "pickups" (collect from customer/supplier location) —
        // no X3 data source wired up for this yet.
        List<RoutePlannerStopDTO> pickups = List.of();

        // ── 10. Build response ─────────────────────────────────
        RoutePlannerResponseDTO response = new RoutePlannerResponseDTO();
        response.setSiteCode(siteCode);
        response.setSiteName(site.getSiteName());
        response.setPlanDate(planDate);
        response.setSite(mapSite(site));
        response.setVehicles(vehicles.stream().map(this::mapVehicle).toList());
        response.setDrivers(drivers.stream().map(this::mapDriver).toList());
        response.setDrops(drops);
        response.setPickups(pickups);
        response.setVehicleCount(vehicles.size());
        response.setDriverCount(drivers.size());
        response.setDropCount(drops.size());
        response.setPickupCount(pickups.size());

        log.info("RoutePlanner: response built — {} vehicles, {} drivers, {} drops ({} deliveries + {} pick tickets), {} pickups",
                vehicles.size(), drivers.size(), drops.size(), deliveries.size(), pickTickets.size(), pickups.size());

        return response;
    }

    // ─────────────────────────────────────────────────────────
    // FETCH X3 STOPS (with error isolation)
    // ─────────────────────────────────────────────────────────
    private List<RoutePlannerStopDTO> fetchX3Stops(
            java.util.function.Supplier<List<RoutePlannerStopDTO>> fetcher,
            String label) {
        try {
            List<RoutePlannerStopDTO> result = fetcher.get();
            log.info("RoutePlanner: {} {} fetched from X3", result.size(), label);
            return result;
        } catch (Exception e) {
            log.error("RoutePlanner: failed to fetch {} from X3 — {}", label, e.getMessage());
            return List.of();
        }
    }

    // ─────────────────────────────────────────────────────────
    // ENRICH STOPS — in-memory join by BPCODE + ADRESCODE
    //
    // 1. Collect all distinct BPCODE values from the stop list
    // 2. One batch query to Postgres: vw_rp_stop_enrich
    //    WHERE customer_code IN (:codes) AND doc_type = :docType
    // 3. Build lookup map: "customerCode::addressCode" → enrichment
    // 4. For each stop: lookup and copy p-fields onto the DTO
    // ─────────────────────────────────────────────────────────
    private void enrichStops(List<RoutePlannerStopDTO> stops, String docType) {

        if (stops.isEmpty()) return;

        // Step 1 — collect distinct BP codes
        List<String> bpCodes = stops.stream()
                .map(RoutePlannerStopDTO::getBpCode)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();

        try {
            // Step 2 — single batch query to Postgres
            List<StopEnrichment> enrichList =
                    enrichmentRepository.findByCustomerCodesAndDocType(bpCodes, docType);

            log.info("RoutePlanner: {} enrichment rows from Postgres for docType={}",
                    enrichList.size(), docType);

            // Step 3 — build lookup map
            Map<String, StopEnrichment> enrichMap = enrichList.stream()
                    .collect(Collectors.toMap(
                            e -> e.getCustomerCode() + "::" + e.getAddressCode(),
                            e -> e,
                            (a, b) -> a   // keep first if duplicate
                    ));

            // Step 4 — merge p-fields onto each stop DTO
            stops.forEach(stop -> {
                String key = stop.getBpCode() + "::" + stop.getAddressCode();
                StopEnrichment e = enrichMap.get(key);
                if (e == null) {
                    log.debug("RoutePlanner: no enrichment for key={}", key);
                    return;
                }

                // Geo
                stop.setLatitude(e.getLatitude());
                stop.setLongitude(e.getLongitude());

                // Service / waiting time
                stop.setServiceTime(e.getServiceTime());
                stop.setWaitingTime(e.getWaitingTime());

                // Time windows
                stop.setAnyTimeWindow(e.getAnyTimeWindow());
                stop.setFromTime(e.getFromTime());
                stop.setToTime(e.getToTime());

                // Route config (doc-type level)
                stop.setRouteTag(e.getRouteTag());
                stop.setRouteTagFra(e.getRouteTagFra());
                stop.setRouteColor(e.getRouteColor());
            });

        } catch (Exception e) {
            log.error("RoutePlanner: Postgres enrichment failed for docType={} — {}",
                    docType, e.getMessage());
            // Stops still returned — just without p-fields (lat/lon will be null)
        }
    }

    // ─────────────────────────────────────────────────────────
    // MAPPERS
    // ─────────────────────────────────────────────────────────
    private RoutePlannerSiteDTO mapSite(XRSite s) {
        RoutePlannerSiteDTO dto = new RoutePlannerSiteDTO();
        dto.setSiteCode(s.getSiteCode());
        dto.setSiteName(s.getSiteName());
        dto.setLatitude(s.getLatitude());
        dto.setLongitude(s.getLongitude());
        dto.setTmsFlag(s.getTmsFlag());
        dto.setAddressLine1(s.getAddressLine1());
        dto.setCity(s.getCity());
        dto.setPostalCode(s.getPostalCode());
        dto.setCountryCode(s.getCountryCode());
        return dto;
    }

    private RoutePlannerVehicleDTO mapVehicle(Vehicle v) {
        RoutePlannerVehicleDTO dto = new RoutePlannerVehicleDTO();
        dto.setVehicleCode(v.getVehicleCode());
        dto.setVehicleName(v.getVehicleName());
        dto.setVehicleNumber(v.getVehicleNumber());
        dto.setBrand(v.getBrand());
        dto.setModel(v.getModel());
        dto.setVehicleYear(v.getVehicleYear());
        dto.setColor(v.getColor());
        dto.setCapacityWeight(v.getCapacityWeight());
        dto.setCapacityVolume(v.getCapacityVolume());
        dto.setVolumeUnit(v.getVolumeUnit());
        dto.setWeightUnit(v.getWeightUnit());
        // Site / Depot
        dto.setSite(v.getSite());
        dto.setDepartureSite(v.getDepartureSite());
        dto.setArrivalSite(v.getArrivalSite());
        dto.setEarliestStartTime(v.getEarliestStartTime());
        // Cost
        dto.setFixedCost(v.getFixedCost());
        dto.setCostPerTime(v.getCostPerTime());
        dto.setCostPerDistance(v.getCostPerDistance());
        // Constraints
        dto.setMaxOrderCount(v.getMaxOrderCount());
        dto.setMaxTotalTime(v.getMaxTotalTime());
        dto.setMaxTotalDistance(v.getMaxTotalDistance());
        // Driver
        dto.setDriverId(v.getDriverId());
        // Image
        if (v.getVehicleImage() != null && v.getVehicleImage().length > 0) {
            dto.setImage("data:image/jpeg;base64,"
                    + java.util.Base64.getEncoder().encodeToString(v.getVehicleImage()));
        }
        // Category
        if (v.getCategory() != null) {
            dto.setCategoryCode(v.getCategory().getCategoryCode());
            dto.setCategoryDescription(v.getCategory().getDescription());
        }
        dto.setVehicleStatus(v.getVehicleStatus());
        dto.setActive(v.getActive());
        return dto;
    }

    private RoutePlannerDriverDTO mapDriver(Driver d) {
        RoutePlannerDriverDTO dto = new RoutePlannerDriverDTO();
        dto.setDriverId(d.getDriverId());
        dto.setDriverName(d.getDriverName());
        dto.setEmployeeCode(d.getEmployeeCode());
        dto.setMobileNo(d.getMobileNo());
        dto.setLicenseNumber(d.getLicenseNumber());
        dto.setLicenseType(d.getLicenseType());
        dto.setDriverStatus(d.getDriverStatus());
        dto.setLongHaulDriver(d.getLongHaulDriver());
        dto.setAllowAllVehicles(d.getAllowAllVehicles());
        return dto;
    }
    // ── 8. Batch-load product lines and attach to stops ──────
    private void loadProducts(List<RoutePlannerStopDTO> stops, String stopType) {
        if (stops == null || stops.isEmpty()) return;

        List<String> docNums = stops.stream()
                .map(RoutePlannerStopDTO::getDocNum)
                .filter(d -> d != null && !d.isBlank())
                .distinct()
                .collect(Collectors.toList());

        if (docNums.isEmpty()) return;

        List<StopProductDTO> allLines = "DROP".equals(stopType)
                ? productRepository.findDeliveryLinesByDocs(docNums)
                : productRepository.findPickupLinesByDocs(docNums);

        Map<String, List<StopProductDTO>> byDoc = allLines.stream()
                .collect(Collectors.groupingBy(StopProductDTO::getDocNum));

        for (RoutePlannerStopDTO stop : stops) {
            stop.setProducts(byDoc.getOrDefault(stop.getDocNum(), List.of()));
        }
    }


}
