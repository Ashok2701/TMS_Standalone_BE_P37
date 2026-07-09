package com.transport.tms.Trip.Impl;

import com.transport.tms.Trip.Dto.TripRequestDTO;
import com.transport.tms.Trip.Dto.TripResponseDTO;
import com.transport.tms.Trip.Dto.TripStatusDTO;
import com.transport.tms.Trip.Entity.XrTrip;
import com.transport.tms.Trip.Repository.TripRepository;
import com.transport.tms.Trip.Service.TripService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transport.tms.Trip.Dto.OptimisationRequestDTO;
import com.transport.tms.Config.SchemaConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TripServiceImpl implements TripService {

    private final TripRepository repo;
    private final ObjectMapper   objectMapper;
    private final SchemaConfig   schemas;

    @Qualifier("sqlServerJdbcTemplate")
    private final JdbcTemplate   sqlServerJdbc;

    // ── CREATE ────────────────────────────────────────────────
    @Override
    public TripResponseDTO createTrip(TripRequestDTO req) {
        XrTrip trip = new XrTrip();
        mapRequestToEntity(req, trip);

        // Auto-generate trip_code: VR-{SITE}-{YYYYMMDD}-{001}
        // e.g. VR-KCC01-20260624-001
        int nextSeq = repo.findMaxStartIndex(req.getSite(), req.getDocDate()) + 1;
        String date = req.getDocDate().format(DateTimeFormatter.BASIC_ISO_DATE);
        String code = String.format("VR-%s-%s-%03d", req.getSite(), date, nextSeq);
        trip.setTripCode(code);
        trip.setStartIndex(nextSeq);
        trip.setStops((req.getDrops() == null ? 0 : req.getDrops())
                    + (req.getPickups() == null ? 0 : req.getPickups()));
        trip.setOptiStatus("Open");
        trip.setLockFlag(0);

        XrTrip saved = repo.save(trip);

        // ── Update XX10TRIPS optistatus in X3 ────────────────
        try {
            String x3 = schemas.getX3Schema();
            sqlServerJdbc.update(
                "UPDATE " + x3 + ".XX10TRIPS SET optistatus = ? WHERE TRIPCODE = ?",
                "Optimized", saved.getTripCode()
            );
        } catch (Exception e) {
            System.err.println("Warning: XX10TRIPS update failed for " + saved.getTripCode() + ": " + e.getMessage());
        }

        return toDTO(saved);
    }

    // ── READ ALL by site + date ───────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<TripResponseDTO> getTripsBySiteAndDate(String site, LocalDate docDate) {
        return repo.findBySiteAndDocDateOrderByCreateDateAsc(site, docDate)
                   .stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ── READ ALL by site ──────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<TripResponseDTO> getTripsBySite(String site) {
        return repo.findBySiteOrderByDocDateDescCreateDateAsc(site)
                   .stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ── READ ONE ──────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public TripResponseDTO getTripById(Long id) {
        return toDTO(findOrThrow(id));
    }

    // ── UPDATE full ───────────────────────────────────────────
    @Override
    public TripResponseDTO updateTrip(Long id, TripRequestDTO req) {
        XrTrip trip = findOrThrow(id);
        mapRequestToEntity(req, trip);
        trip.setStops((req.getDrops() == null ? 0 : req.getDrops())
                    + (req.getPickups() == null ? 0 : req.getPickups()));
        return toDTO(repo.save(trip));
    }

    // ── PATCH status (lock / validate / open) ────────────────
    @Override
    public TripResponseDTO updateStatus(Long id, TripStatusDTO dto) {
        XrTrip trip = findOrThrow(id);
        if (dto.getOptiStatus() != null) trip.setOptiStatus(dto.getOptiStatus());
        if (dto.getLockFlag()   != null) trip.setLockFlag(dto.getLockFlag());
        if (dto.getNotes()      != null) trip.setNotes(dto.getNotes());
        if (dto.getUserCode()   != null) trip.setUserCode(dto.getUserCode());
        return toDTO(repo.save(trip));
    }

    // ── PATCH optimise ────────────────────────────────────────
    @Override
    @SuppressWarnings("unchecked")
    public TripResponseDTO optimiseTrip(Long id, OptimisationRequestDTO req) {
        XrTrip trip = findOrThrow(id);

        // ── Status & settings ─────────────────────────────────
        trip.setOptiStatus("Optimised");
        trip.setHeuExec(req.getOrderMode() != null ? req.getOrderMode() : "fixed");
        trip.setDatExec(OffsetDateTime.now());

        // Store totalObject if provided
        if (req.getTotalObject() != null) trip.setTotalObjectJson(toJson(req.getTotalObject()));
        if (req.getStartTime()    != null) trip.setStartTime(req.getStartTime());
        if (req.getEndTime()      != null) trip.setEndTime(req.getEndTime());
        if (req.getTravelTime()   != null) trip.setTravelTime(req.getTravelTime());
        if (req.getTotalTime()    != null) trip.setTotalTime(req.getTotalTime());
        if (req.getServiceTime()  != null) trip.setServiceTime(req.getServiceTime());
        if (req.getTotalDistance()!= null) trip.setTotalDistance(req.getTotalDistance());
        if (req.getUomDistance()  != null) trip.setUomDistance(req.getUomDistance());
        if (req.getTotalCost()    != null) trip.setTotalCost(req.getTotalCost());
        if (req.getDistanceCost() != null) trip.setDistanceCost(req.getDistanceCost());
        if (req.getFixedCost()    != null) trip.setFixedCost(req.getFixedCost());
        if (req.getServiceCost()  != null) trip.setServiceCost(req.getServiceCost());
        if (req.getRegularCost()  != null) trip.setRegularCost(req.getRegularCost());
        if (req.getOvertimeCost() != null) trip.setOvertimeCost(req.getOvertimeCost());

        // ── Merge per-stop results into stopObjects JSONB ─────
        if (req.getStopResults() != null && !req.getStopResults().isEmpty()
                && trip.getStopObjectsJson() != null) {
            try {
                // Parse stopObjectsJson string to List<Map>
                List<java.util.Map<String, Object>> stopList =
                    objectMapper.readValue(
                        trip.getStopObjectsJson() != null ? trip.getStopObjectsJson() : "[]",
                        objectMapper.getTypeFactory().constructCollectionType(
                            List.class, java.util.Map.class));

                // Build a lookup map: docNum → StopOptimisationResult
                java.util.Map<String, OptimisationRequestDTO.StopOptimisationResult> resultMap
                    = new java.util.HashMap<>();
                for (OptimisationRequestDTO.StopOptimisationResult r : req.getStopResults()) {
                    if (r.getDocNum() != null) resultMap.put(r.getDocNum(), r);
                }

                // Merge each stop result by matching docNum OR by seq index
                for (int i = 0; i < stopList.size(); i++) {
                    java.util.Map<String, Object> stop = stopList.get(i);

                    // Try match by docNum first, then by seq (1-based)
                    String docNum = stop.getOrDefault("txn",
                                   stop.getOrDefault("docNum",
                                   stop.getOrDefault("id", ""))).toString();

                    OptimisationRequestDTO.StopOptimisationResult r = resultMap.get(docNum);
                    if (r == null) {
                        // Fallback: match by seq position (i+1)
                        for (OptimisationRequestDTO.StopOptimisationResult sr : req.getStopResults()) {
                            if (sr.getSeq() != null && sr.getSeq() == i + 1) { r = sr; break; }
                        }
                    }

                    if (r != null) {
                        // Arrival / Departure
                        if (r.getArrivalDate()        != null) stop.put("arrivalDate",        r.getArrivalDate());
                        if (r.getArrivalTime()        != null) stop.put("arrivalTime",        r.getArrivalTime());
                        if (r.getDepartureDate()      != null) stop.put("departureDate",      r.getDepartureDate());
                        if (r.getDepartureTime()      != null) stop.put("departureTime",      r.getDepartureTime());
                        // Distance / travel from previous stop
                        if (r.getFromPrevDistance()   != null) stop.put("fromPrevDistance",   r.getFromPrevDistance());
                        if (r.getFromPrevTravelTime() != null) stop.put("fromPrevTravelTime", r.getFromPrevTravelTime());
                        // At-stop metrics
                        if (r.getServiceTime()        != null) stop.put("serviceTime",        r.getServiceTime());
                        if (r.getWaitingTime()        != null) stop.put("waitingTime",        r.getWaitingTime());
                        // Sequence position
                        stop.put("seq", i + 1);
                    }
                }

                // Re-serialize updated stop list back to JSON string
                trip.setStopObjectsJson(objectMapper.writeValueAsString(stopList));

            } catch (Exception e) {
                // Log but don't fail the whole request
                System.err.println("Warning: could not merge stop optimisation results: " + e.getMessage());
            }
        }

        return toDTO(repo.save(trip));
    }

    // ── DELETE ────────────────────────────────────────────────
    @Override
    public void deleteTrip(Long id) {
        XrTrip trip = findOrThrow(id);
        repo.delete(trip);
    }

    // ── Helpers ───────────────────────────────────────────────
    private XrTrip findOrThrow(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Trip not found: " + id));
    }

    // ── JSON helpers ─────────────────────────────────────────
    private String toJson(Object obj) {
        if (obj == null) return null;
        try { return objectMapper.writeValueAsString(obj); }
        catch (Exception e) { return null; }
    }

    @SuppressWarnings("unchecked")
    private List<Object> fromJsonList(String json) {
        if (json == null || json.isBlank()) return null;
        try { return objectMapper.readValue(json, List.class); }
        catch (Exception e) { return null; }
    }

    @SuppressWarnings("unchecked")
    private java.util.Map<String,Object> fromJsonMap(String json) {
        if (json == null || json.isBlank()) return null;
        try { return objectMapper.readValue(json, java.util.Map.class); }
        catch (Exception e) { return null; }
    }

    private void mapRequestToEntity(TripRequestDTO req, XrTrip trip) {
        trip.setSite(req.getSite());
        trip.setDocDate(req.getDocDate());
        trip.setDriverId(req.getDriverId());
        trip.setDriverName(req.getDriverName() != null ? req.getDriverName() : "");
        trip.setVehicleCode(req.getVehicleCode());
        trip.setDepSite(req.getDepSite());
        trip.setArrSite(req.getArrSite());
        trip.setDrops(req.getDrops()   != null ? req.getDrops()   : 0);
        trip.setPickups(req.getPickups()!= null ? req.getPickups() : 0);
        trip.setNoOfPackages(req.getNoOfPackages());
        trip.setStartTime(req.getStartTime());
        trip.setEndTime(req.getEndTime());
        trip.setTravelTime(req.getTravelTime());
        trip.setTotalTime(req.getTotalTime());
        trip.setServiceTime(req.getServiceTime());
        trip.setTotalWeight(req.getTotalWeight());
        trip.setTotalVolume(req.getTotalVolume());
        trip.setCapacity(req.getCapacity());
        trip.setUomCapacity(req.getUomCapacity());
        trip.setUomVolume(req.getUomVolume());
        trip.setUomTime(req.getUomTime());
        trip.setUomDistance(req.getUomDistance());
        trip.setWeightPct(req.getWeightPct());
        trip.setVolumePct(req.getVolumePct());
        trip.setTotalDistance(req.getTotalDistance());
        trip.setTotalCost(req.getTotalCost());
        trip.setDistanceCost(req.getDistanceCost());
        trip.setFixedCost(req.getFixedCost());
        trip.setServiceCost(req.getServiceCost());
        trip.setRegularCost(req.getRegularCost());
        trip.setOvertimeCost(req.getOvertimeCost());
        trip.setNotes(req.getNotes());
        trip.setGeneratedBy(req.getGeneratedBy() != null ? req.getGeneratedBy() : "PLANNER");
        trip.setForceSeq(req.getForceSeq() != null ? req.getForceSeq() : 0);
        trip.setVrSeq(req.getVrSeq());
        // stopObjects: array of all stops in order (drops + pickups)
        // Serialize JSONB objects to JSON strings for Postgres
        trip.setStopObjectsJson(toJson(req.getStopObjects()));
        trip.setVehicleObjectJson(toJson(req.getVehicleObject()));
        trip.setTotalObjectJson(toJson(req.getTotalObject()));
        trip.setTotCapacity(req.getTotCapacity());
        trip.setTotVolumeCap(req.getTotVolumeCap());
        trip.setDocCapacity(req.getDocCapacity());
        trip.setDocVolume(req.getDocVolume());
        trip.setPerCapacity(req.getPerCapacity());
        trip.setPerVolume(req.getPerVolume());
        trip.setDocQty(req.getDocQty());
        trip.setUomQty(req.getUomQty());
        trip.setMaxPalletCnt(req.getMaxPalletCnt());
        trip.setUserCode(req.getUserCode() != null ? req.getUserCode() : "SYSTEM");
    }

    private TripResponseDTO toDTO(XrTrip t) {
        TripResponseDTO dto = new TripResponseDTO();
        dto.setTripId(t.getTripId());
        dto.setTripCode(t.getTripCode());
        dto.setSite(t.getSite());
        dto.setDocDate(t.getDocDate());
        dto.setDriverId(t.getDriverId());
        dto.setDriverName(t.getDriverName());
        dto.setVehicleCode(t.getVehicleCode());
        dto.setStops(t.getStops());
        dto.setDrops(t.getDrops());
        dto.setPickups(t.getPickups());
        dto.setNoOfPackages(t.getNoOfPackages());
        dto.setDepSite(t.getDepSite());
        dto.setArrSite(t.getArrSite());
        dto.setStartTime(t.getStartTime());
        dto.setEndTime(t.getEndTime());
        dto.setTravelTime(t.getTravelTime());
        dto.setTotalTime(t.getTotalTime());
        dto.setServiceTime(t.getServiceTime());
        dto.setTotalWeight(t.getTotalWeight());
        dto.setTotalVolume(t.getTotalVolume());
        dto.setCapacity(t.getCapacity());
        dto.setUomCapacity(t.getUomCapacity());
        dto.setUomVolume(t.getUomVolume());
        dto.setUomTime(t.getUomTime());
        dto.setUomDistance(t.getUomDistance());
        dto.setWeightPct(t.getWeightPct());
        dto.setVolumePct(t.getVolumePct());
        dto.setTotalDistance(t.getTotalDistance());
        dto.setTotalCost(t.getTotalCost());
        dto.setDistanceCost(t.getDistanceCost());
        dto.setFixedCost(t.getFixedCost());
        dto.setServiceCost(t.getServiceCost());
        dto.setOptiStatus(t.getOptiStatus());
        dto.setLockFlag(t.getLockFlag());
        dto.setForceSeq(t.getForceSeq());
        dto.setVrSeq(t.getVrSeq());
        dto.setNotes(t.getNotes());
        dto.setGeneratedBy(t.getGeneratedBy());
        // Deserialize JSON strings back to objects for response
        dto.setStopObjects(fromJsonList(t.getStopObjectsJson()));
        dto.setVehicleObject(fromJsonMap(t.getVehicleObjectJson()));
        dto.setTotalObject(fromJsonMap(t.getTotalObjectJson()));
        dto.setPerCapacity(t.getPerCapacity());
        dto.setPerVolume(t.getPerVolume());
        dto.setDocQty(t.getDocQty());
        dto.setAlertFlag(t.getAlertFlag());
        dto.setWarningNotes(t.getWarningNotes());
        dto.setUserCode(t.getUserCode());
        dto.setCreateDate(t.getCreateDate());
        dto.setUpdateDate(t.getUpdateDate());
        return dto;
    }
    // ── Write trip to XX10TRIPS (X3 SQL Server) ──────────────
    private void writeX3Trip(XrTrip trip) {
        String x3 = schemas.getX3Schema();
        // Upsert: delete + insert
        sqlServerJdbc.update("DELETE FROM " + x3 + ".XX10TRIPS WHERE TRIPCODE = ?", trip.getTripCode());
        sqlServerJdbc.update(
            "INSERT INTO " + x3 + ".XX10TRIPS (TRIPCODE, optistatus, lock, FCY_0, DATLIV_0, VEHCODE, DRIVERID) VALUES (?,?,?,?,?,?,?)",
            trip.getTripCode(),
            "Open",
            0,
            trip.getSite(),
            trip.getDocDate(),
            trip.getVehicleCode(),
            trip.getDriverId()
        );
    }

    // ── Update SDELIVERY + STOPREH with trip code ─────────────
    @SuppressWarnings("unchecked")
    private void updateStopDocuments(XrTrip trip) {
        if (trip.getStopObjectsJson() == null) return;
        String x3 = schemas.getX3Schema();
        String code = trip.getTripCode();

        try {
            java.util.List<java.util.Map<String, Object>> stops = objectMapper.readValue(
                trip.getStopObjectsJson(),
                objectMapper.getTypeFactory().constructCollectionType(java.util.List.class, java.util.Map.class)
            );

            for (java.util.Map<String, Object> stop : stops) {
                String docNum = getString(stop, "txn", "docNum", "id");
                String type   = getString(stop, "type", "stopType");
                if (docNum == null) continue;

                if ("PICKUP".equals(type)) {
                    // Update pickup ticket
                    sqlServerJdbc.update(
                        "UPDATE " + x3 + ".STOPREH SET XX10C_NUMPC_0 = ? WHERE VCRNUM_0 = ?",
                        code, docNum
                    );
                } else {
                    // Update delivery
                    sqlServerJdbc.update(
                        "UPDATE " + x3 + ".SDELIVERY SET XX10C_NUMPC_0 = ? WHERE SDHNUM_0 = ?",
                        code, docNum
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("Warning: stop document update failed: " + e.getMessage());
        }
    }

    private String getString(java.util.Map<String, Object> m, String... keys) {
        for (String k : keys) {
            Object v = m.get(k);
            if (v != null) return v.toString();
        }
        return null;
    }


}
