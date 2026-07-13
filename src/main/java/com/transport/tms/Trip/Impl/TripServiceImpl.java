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
@Transactional
public class TripServiceImpl implements TripService {

    private final TripRepository repo;
    private final ObjectMapper   objectMapper;
    private final SchemaConfig   schemas;
    private final JdbcTemplate   sqlServerJdbc;

    public TripServiceImpl(
            TripRepository repo,
            ObjectMapper objectMapper,
            SchemaConfig schemas,
            @Qualifier("sqlServerJdbcTemplate") JdbcTemplate sqlServerJdbc) {
        this.repo           = repo;
        this.objectMapper   = objectMapper;
        this.schemas        = schemas;
        this.sqlServerJdbc  = sqlServerJdbc;
    }

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

        // ── X3 writes on CONFIRM ──────────────────────────────
        // 1. Insert XX10TRIPS
        try { writeX3Trip(saved); }
        catch (Exception e) { System.err.println("X3 XX10TRIPS insert failed: " + e.getMessage()); }

        // 2. Update SDELIVERY/STOPREH: trip code + driver + vehicle + status=1
        try { updateStopDocuments(saved); }
        catch (Exception e) { System.err.println("X3 stop documents failed: " + e.getMessage()); }

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
    public TripResponseDTO getTripById(String tripCode) {
        return toDTO(findOrThrow(tripCode));
    }

    // ── UPDATE full ───────────────────────────────────────────
    @Override
    public TripResponseDTO updateTrip(String tripCode, TripRequestDTO req) {
        XrTrip trip = findOrThrow(tripCode);
        mapRequestToEntity(req, trip);
        trip.setStops((req.getDrops() == null ? 0 : req.getDrops())
                    + (req.getPickups() == null ? 0 : req.getPickups()));
        return toDTO(repo.save(trip));
    }

    // ── PATCH status (lock / validate / open) ────────────────
    @Override
    public TripResponseDTO updateStatus(String tripCode, TripStatusDTO dto) {
        XrTrip trip = findOrThrow(tripCode);
        if (dto.getOptiStatus() != null) trip.setOptiStatus(dto.getOptiStatus());
        if (dto.getLockFlag()   != null) trip.setLockFlag(dto.getLockFlag());
        if (dto.getNotes()      != null) trip.setNotes(dto.getNotes());
        if (dto.getUserCode()   != null) trip.setUserCode(dto.getUserCode());
        return toDTO(repo.save(trip));
    }

    // ── PATCH optimise ────────────────────────────────────────
    @Override
    @SuppressWarnings("unchecked")
    public TripResponseDTO optimiseTrip(String tripCode, OptimisationRequestDTO req) {
        XrTrip trip = findOrThrow(tripCode);

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
                System.err.println("Warning: could not merge stop optimisation results: " + e.getMessage());
            }
        }

        XrTrip saved = repo.save(trip);

        // ── X3 writes on OPTIMISE ─────────────────────────────
        // 3. Update XX10TRIPS.optistatus = Optimized
        try {
            String x3 = schemas.getX3Schema();
            sqlServerJdbc.update(
                "UPDATE " + x3 + ".XX10TRIPS SET optistatus = ? WHERE TRIPCODE = ?",
                "Optimized", saved.getTripCode()
            );
        } catch (Exception e) { System.err.println("X3 XX10TRIPS optimise failed: " + e.getMessage()); }

        // 4. Update SDELIVERY/STOPREH: arrival + departure time per stop
        try { updateStopTimes(saved); }
        catch (Exception e) { System.err.println("X3 stop times failed: " + e.getMessage()); }

        return toDTO(saved);
    }

    // ── DELETE ────────────────────────────────────────────────
    @Override
    public void deleteTrip(String tripCode) {
        XrTrip trip = findOrThrow(tripCode);

        // Release the trip's stop documents back to the open pool in X3
        // (mirrors CBTTL: on trip delete, SDELIVERY/STOPREH XDLV_STATUS_0
        // is reset to 8 — released/unassigned — so the doc can be picked up
        // by another trip). Previously this was skipped entirely, leaving
        // deleted trips' docs stuck at XDLV_STATUS_0 = 1 (Allocated) with a
        // dangling XNUMPC_0 reference to a trip code that no longer exists.
        try { releaseStopDocuments(trip); }
        catch (Exception e) { System.err.println("X3 stop document release failed: " + e.getMessage()); }

        repo.delete(trip);
    }

    // ── Release SDELIVERY + STOPREH on DELETE ──────────────────
    // Resets: status = 8, clears trip code / driver / vehicle /
    //         departure date+time / arrival date+time
    @SuppressWarnings("unchecked")
    private void releaseStopDocuments(XrTrip trip) {
        if (trip.getStopObjectsJson() == null) return;
        String x3 = schemas.getX3Schema();

        try {
            java.util.List<java.util.Map<String, Object>> stops = objectMapper.readValue(
                trip.getStopObjectsJson(),
                objectMapper.getTypeFactory().constructCollectionType(java.util.List.class, java.util.Map.class)
            );

            for (java.util.Map<String, Object> stop : stops) {
                String docNum = getString(stop, "txn", "docNum", "id");
                if (docNum == null) continue;

                if (isPickTicket(stop)) {
                    sqlServerJdbc.update(
                        "UPDATE " + x3 + ".STOPREH SET "
                            + "XNUMPC_0 = '', "     // clear trip code
                            + "XDRIVER_0 = '', "    // clear driver id
                            + "CODEYVE_0 = '', "    // clear vehicle code
                            + "XDLV_STATUS_0 = 8, " // status = 8 (deleted/released)
                            + "DPEDAT_0 = NULL, "   // clear departure date
                            + "ETD_0 = '', "        // clear departure time
                            + "ARVDAT_0 = NULL, "   // clear arrival date
                            + "ETA_0 = '' "         // clear arrival time
                            + "WHERE PRHNUM_0 = ?",
                        docNum
                    );
                } else {
                    sqlServerJdbc.update(
                        "UPDATE " + x3 + ".SDELIVERY SET "
                            + "XNUMPC_0 = '', "
                            + "XDRIVER_0 = '', "
                            + "CODEYVE_0 = '', "
                            + "XDLV_STATUS_0 = 8, "
                            + "DPEDAT_0 = NULL, "
                            + "ETD_0 = '', "
                            + "ARVDAT_0 = NULL, "
                            + "ETA_0 = '' "
                            + "WHERE SDHNUM_0 = ?",
                        docNum
                    );
                }
            }
            System.out.println("X3 stop documents released for trip: " + trip.getTripCode());
        } catch (Exception e) {
            System.err.println("Warning: stop document release failed: " + e.getMessage());
        }
    }

    // ── Helpers ───────────────────────────────────────────────
    private XrTrip findOrThrow(String tripCode) {
        return repo.findByTripCode(tripCode)
                .orElseThrow(() -> new RuntimeException("Trip not found: " + tripCode));
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

    // ── Update SDELIVERY + STOPREH on CONFIRM ─────────────────
    // Sets: trip code, driver, vehicle, delivery status = 1 (Allocated)
    @SuppressWarnings("unchecked")
    private void updateStopDocuments(XrTrip trip) {
        if (trip.getStopObjectsJson() == null) return;
        String x3     = schemas.getX3Schema();
        String code   = trip.getTripCode();
        String driver = trip.getDriverId();
        String veh    = trip.getVehicleCode();

        try {
            java.util.List<java.util.Map<String, Object>> stops = objectMapper.readValue(
                trip.getStopObjectsJson(),
                objectMapper.getTypeFactory().constructCollectionType(java.util.List.class, java.util.Map.class)
            );

            for (java.util.Map<String, Object> stop : stops) {
                String docNum = getString(stop, "txn", "docNum", "id");
                if (docNum == null) continue;

                if (isPickTicket(stop)) {
                    sqlServerJdbc.update(
                        "UPDATE " + x3 + ".STOPREH SET "
                            + "XNUMPC_0 = ?, "       // trip code
                            + "XDRIVER_0 = ?, "      // driver id
                            + "CODEYVE_0 = ?, "      // vehicle code
                            + "XDLV_STATUS_0 = 1 "    // status = Allocated
                            + "WHERE PRHNUM_0 = ?",
                        code, driver, veh, docNum
                    );
                } else {
                    sqlServerJdbc.update(
                        "UPDATE " + x3 + ".SDELIVERY SET "
                            + "XNUMPC_0 = ?, "       // trip code
                            + "XDRIVER_0 = ?, "      // driver id
                            + "CODEYVE_0 = ?, "      // vehicle code
                            + "XDLV_STATUS_0 = 1 "    // status = Allocated
                            + "WHERE SDHNUM_0 = ?",
                        code, driver, veh, docNum
                    );
                }
            }
            System.out.println("X3 stop documents updated for trip: " + code);
        } catch (Exception e) {
            System.err.println("Warning: stop document update failed: " + e.getMessage());
        }
    }

    // ── Update SDELIVERY + STOPREH on OPTIMISE ─────────────────
    // Sets: departure date+time, arrival date+time per stop
    //
    // BUG FIXED: this used to write "XDEPTIME_0"/"XARVTIME_0" — those
    // aren't real columns on SDELIVERY/STOPREH. Per XTMSDLVY_TMS_view.sql
    // and XTMSPICK_TMS_view.sql, the actual native X3 columns are:
    //   DPEDAT_0 (departure date), ETD_0 (departure time),
    //   ARVDAT_0 (arrival date),   ETA_0 (arrival time)
    // and no date was ever being written at all before this fix.
    @SuppressWarnings("unchecked")
    private void updateStopTimes(XrTrip trip) {
        if (trip.getStopObjectsJson() == null) return;
        String x3 = schemas.getX3Schema();

        try {
            java.util.List<java.util.Map<String, Object>> stops = objectMapper.readValue(
                trip.getStopObjectsJson(),
                objectMapper.getTypeFactory().constructCollectionType(java.util.List.class, java.util.Map.class)
            );

            for (java.util.Map<String, Object> stop : stops) {
                String docNum   = getString(stop, "txn", "docNum", "id");
                String depDate  = getString(stop, "departureDate");
                String depTime  = getString(stop, "departureTime");
                String arrDate  = getString(stop, "arrivalDate");
                String arrTime  = getString(stop, "arrivalTime");
                if (docNum == null) continue;

                java.sql.Date depDateSql = toSqlDate(depDate);
                java.sql.Date arrDateSql = toSqlDate(arrDate);

                if (isPickTicket(stop)) {
                    sqlServerJdbc.update(
                        "UPDATE " + x3 + ".STOPREH SET "
                            + "DPEDAT_0 = ?, "      // departure date
                            + "ETD_0 = ?, "         // departure time HH:MM
                            + "ARVDAT_0 = ?, "      // arrival date
                            + "ETA_0 = ? "          // arrival time HH:MM
                            + "WHERE PRHNUM_0 = ?",
                        depDateSql, depTime, arrDateSql, arrTime, docNum
                    );
                } else {
                    sqlServerJdbc.update(
                        "UPDATE " + x3 + ".SDELIVERY SET "
                            + "DPEDAT_0 = ?, "
                            + "ETD_0 = ?, "
                            + "ARVDAT_0 = ?, "
                            + "ETA_0 = ? "
                            + "WHERE SDHNUM_0 = ?",
                        depDateSql, depTime, arrDateSql, arrTime, docNum
                    );
                }
            }
            System.out.println("X3 stop times updated for trip: " + trip.getTripCode());
        } catch (Exception e) {
            System.err.println("Warning: stop time update failed: " + e.getMessage());
        }
    }

    // Parses an ISO date string ("yyyy-MM-dd", optionally with a time
    // component) into a java.sql.Date; returns null (not today's date)
    // on blank/unparseable input so callers can safely write NULL.
    private java.sql.Date toSqlDate(String date) {
        if (date == null || date.isBlank()) return null;
        try {
            String d = date.trim();
            if (d.length() > 10) d = d.substring(0, 10);
            return java.sql.Date.valueOf(java.time.LocalDate.parse(d));
        } catch (Exception e) {
            return null;
        }
    }

    private String getString(java.util.Map<String, Object> m, String... keys) {
        for (String k : keys) {
            Object v = m.get(k);
            if (v != null) return v.toString();
        }
        return null;
    }

    // BUG FIX: callers used to branch on "type"/"stopType", which is now
    // ALWAYS "DROP" for every stop (business rule: pick tickets are a
    // kind of drop — see X3RoutePlannerRepository.mapStop()). That made
    // every pick-ticket stop silently fall into the SDELIVERY branch
    // instead of STOPREH, so CONFIRM/OPTIMISE/DELETE affected 0 rows for
    // pick tickets (no matching SDHNUM_0 for a PIC-prefixed doc number).
    // "docType"/"doctype" ("DLV"/"PICK") still reliably identifies the
    // underlying source table — it's untouched by the Drops/Pickups
    // business-bucket reclassification.
    private boolean isPickTicket(java.util.Map<String, Object> stop) {
        String docType = getString(stop, "docType", "doctype");
        if (docType != null) return "PICK".equalsIgnoreCase(docType);
        // Fallback for any older payloads saved before docType was reliably sent
        return "PICKUP".equals(getString(stop, "type", "stopType"));
    }

}
