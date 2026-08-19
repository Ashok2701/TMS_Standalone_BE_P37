package com.transport.tms.Trip.Lock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transport.tms.Config.SchemaConfig;
import com.transport.tms.Fleet.Entity.Vehicle;
import com.transport.tms.Fleet.Repository.VehicleRepository;
import com.transport.tms.Trip.Entity.XrTrip;
import com.transport.tms.Trip.Lock.Entity.LvsHeader;
import com.transport.tms.Trip.Lock.Entity.VrDetail;
import com.transport.tms.Trip.Lock.Entity.VrHeader;
import com.transport.tms.Trip.Lock.Repository.LvsHeaderRepository;
import com.transport.tms.Trip.Lock.Repository.VrDetailRepository;
import com.transport.tms.Trip.Lock.Repository.VrHeaderRepository;
import com.transport.tms.Trip.Repository.TripRepository;
import com.transport.tms.X3Soap.X3SoapService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lock / Validate / Unlock. As of this version, XX10CPLANCHA/XX10CPLANCHD/
 * XX10CLODSTOH (X3 SQL Server) are no longer written to at all — those
 * tables have been replaced by xr_vrheader/xr_vrdetails/xr_lvsheader on
 * Postgres. TMS owns this data completely now; X3 SQL Server is only
 * still touched here for SDELIVERY/STOPREH (the actual delivery/pickup
 * documents, which remain X3's own data), and the XX10CDOCUP SOAP call
 * (vehicle/driver/sequence/status/trailer/date per document) on Lock.
 */
@Slf4j
@Service
public class TripLockService {

    private final TripRepository       tripRepository;
    private final VrHeaderRepository   vrHeaderRepository;
    private final VrDetailRepository   vrDetailRepository;
    private final LvsHeaderRepository  lvsHeaderRepository;
    private final VehicleRepository    vehicleRepository;
    private final X3SoapService        x3SoapService;
    private final SchemaConfig         schemas;
    private final ObjectMapper         objectMapper;
    private final JdbcTemplate         sqlServerJdbc;

    public TripLockService(
            TripRepository tripRepository,
            VrHeaderRepository vrHeaderRepository,
            VrDetailRepository vrDetailRepository,
            LvsHeaderRepository lvsHeaderRepository,
            VehicleRepository vehicleRepository,
            X3SoapService x3SoapService,
            SchemaConfig schemas,
            ObjectMapper objectMapper,
            @Qualifier("sqlServerJdbcTemplate") JdbcTemplate sqlServerJdbc) {
        this.tripRepository      = tripRepository;
        this.vrHeaderRepository  = vrHeaderRepository;
        this.vrDetailRepository  = vrDetailRepository;
        this.lvsHeaderRepository = lvsHeaderRepository;
        this.vehicleRepository   = vehicleRepository;
        this.x3SoapService       = x3SoapService;
        this.schemas             = schemas;
        this.objectMapper        = objectMapper;
        this.sqlServerJdbc       = sqlServerJdbc;
    }

    // ── LOCK ─────────────────────────────────────────────────
    @Transactional
    public void lockTrip(String tripCode, String userCode) {
        XrTrip trip = findTrip(tripCode);

        if (trip.getLockFlag() != null && trip.getLockFlag() == 1)
            throw new RuntimeException("Trip already locked: " + tripCode);

        // 1. xr_vrheader + xr_vrdetails (Postgres — replaces XX10CPLANCHA/XX10CPLANCHD)
        writeVrHeader(trip, userCode);
        writeVrDetails(trip, userCode);

        // 2. XX10CDOCUP — push vehicle/driver/sequence/status/trailer/date
        //    to X3 for every document on this trip. Non-blocking: an X3
        //    rejection here shouldn't undo the lock that already
        //    succeeded on our own side (same pattern as every other X3
        //    call in this codebase).
        try {
            updateDocumentsInX3(trip);
        } catch (Exception e) {
            log.error("XX10CDOCUP failed for {}: {}", tripCode, e.getMessage());
        }

        // 3. Postgres — trip status itself
        trip.setOptiStatus("Locked");
        trip.setLockFlag(1);
        trip.setDatExec(OffsetDateTime.now());
        tripRepository.save(trip);
        log.info("LOCKED {}", tripCode);
    }

    // ── VALIDATE ──────────────────────────────────────────────
    @Transactional
    public void validateTrip(String tripCode, String userCode) {
        XrTrip trip = findTrip(tripCode);
        if (trip.getLockFlag() == null || trip.getLockFlag() == 0)
            throw new RuntimeException("Trip must be locked before validation: " + tripCode);

        String x3 = schemas.getX3Schema();

        // 1. xr_lvsheader (Postgres — replaces XX10CLODSTOH)
        writeLvsHeader(trip, userCode);

        // 2. SDELIVERY + STOPREH XDLV_STATUS_0 = 2 — still X3's own
        //    delivery/pickup documents, unaffected by this migration.
        updateDocStatusOnValidate(trip, x3);

        // 3. Postgres — trip status itself
        trip.setOptiStatus("Validated");
        tripRepository.save(trip);
        log.info("VALIDATED {}", tripCode);
    }

    // ── UNLOCK ────────────────────────────────────────────────
    @Transactional
    public void unlockTrip(String tripCode, String userCode) {
        XrTrip trip = findTrip(tripCode);
        if ("Validated".equals(trip.getOptiStatus()))
            throw new RuntimeException("Validated trips cannot be unlocked: " + tripCode);

        // 1. XX10CDOCRA — revert every document's status in X3. Non-
        //    blocking, same reasoning as updateDocumentsInX3 on Lock: an
        //    X3-side rejection for one document (e.g. "Pickticket record
        //    is not there") shouldn't prevent the trip from actually
        //    unlocking on our own side.
        try {
            List<String> docNums = extractDocNums(trip);
            if (!docNums.isEmpty()) {
                Map<String, Object> resp = x3SoapService.revertDocumentStatus(docNums);
                log.info("XX10CDOCRA for {}: {}", tripCode, resp);
            }
        } catch (Exception e) {
            log.error("XX10CDOCRA failed for {}: {}", tripCode, e.getMessage());
        }

        // 2. Delete xr_vrheader + xr_vrdetails (Postgres)
        vrDetailRepository.deleteByTripCode(tripCode);
        vrHeaderRepository.findById(tripCode).ifPresent(vrHeaderRepository::delete);

        // 3. Postgres — trip status itself
        trip.setOptiStatus("Optimised");
        trip.setLockFlag(0);
        tripRepository.save(trip);
        log.info("UNLOCKED {}", tripCode);
    }

    // ── LVS CONFIRM ───────────────────────────────────────────
    // Calls X3's XX10CRESDH for every document on the trip, and only if
    // ALL of them succeed, sets xr_lvsheader.confirmed_flag = 1 —
    // enforcing the sequential LVS Create -> LVS Confirm -> Load Truck
    // flow with a real, persisted flag rather than trusting the
    // frontend to remember what state things are in.
    @Transactional
    @SuppressWarnings("unchecked")
    public Map<String, Object> confirmLvs(String tripCode) {
        XrTrip trip = findTrip(tripCode);
        LvsHeader lvs = lvsHeaderRepository.findByTripCode(tripCode)
                .orElseThrow(() -> new RuntimeException("LVS not created yet for " + tripCode + " — complete LVS Create first."));

        if (trip.getStopObjectsJson() == null || trip.getStopObjectsJson().isBlank())
            throw new RuntimeException("Trip has no documents to confirm: " + tripCode);

        List<Map<String, Object>> stops;
        try {
            stops = objectMapper.readValue(trip.getStopObjectsJson(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
        } catch (Exception e) {
            throw new RuntimeException("Cannot read documents for " + tripCode + ": " + e.getMessage());
        }
        List<String> docNums = new ArrayList<>();
        for (Map<String, Object> s : stops) {
            String docNum = getString(s, "txn", "docNum", "id");
            if (docNum != null) docNums.add(docNum);
        }
        if (docNums.isEmpty())
            throw new RuntimeException("Trip has no documents to confirm: " + tripCode);

        Map<String, Object> resp = x3SoapService.confirmDeliveries(docNums);
        if (resp != null && resp.get("error") != null) {
            throw new RuntimeException("X3 confirm failed: " + resp.get("error"));
        }

        List<Map<String, Object>> rows = resp != null ? (List<Map<String, Object>>) resp.getOrDefault("grp1", List.of()) : List.of();
        long failed = rows.stream().filter(r -> !isX3Success(r.get("o_xstatus"), r.get("o_xmess"))).count();

        if (!rows.isEmpty() && failed == 0) {
            lvs.setConfirmedFlag(1);
            lvs.setUpdatedAt(LocalDateTime.now());
            lvsHeaderRepository.save(lvs);
            log.info("LVS CONFIRMED for {} — {} document(s)", tripCode, rows.size());
        } else if (failed > 0) {
            log.warn("LVS confirm partial failure for {} — {}/{} document(s) failed", tripCode, failed, rows.size());
        } else {
            // No table came back at all — fall back to treating the call
            // itself succeeding as confirmation, same lenient handling
            // used elsewhere in this codebase for responses that don't
            // come back in the exact expected shape.
            lvs.setConfirmedFlag(1);
            lvs.setUpdatedAt(LocalDateTime.now());
            lvsHeaderRepository.save(lvs);
            log.info("LVS CONFIRMED for {} (no per-document detail in response)", tripCode);
        }

        return resp;
    }

    // ── LOAD TRUCK ────────────────────────────────────────────
    // Calls X3's XX10CSTOLO for every document on the trip (replaces
    // X10CSTKMTV, a single-LVS-number call), and only sets
    // xr_lvsheader.load_flag = 1 if every document's stock allocation
    // succeeds. Blocks entirely if LVS Confirm hasn't succeeded yet —
    // enforces the sequence server-side rather than relying on the UI to
    // only show the button at the right time.
    @Transactional
    @SuppressWarnings("unchecked")
    public Map<String, Object> loadTruck(String tripCode) {
        XrTrip trip = findTrip(tripCode);
        LvsHeader lvs = lvsHeaderRepository.findByTripCode(tripCode)
                .orElseThrow(() -> new RuntimeException("LVS not created yet for " + tripCode + " — complete LVS Create first."));

        if (lvs.getConfirmedFlag() == null || lvs.getConfirmedFlag() == 0)
            throw new RuntimeException("LVS must be confirmed before loading the truck: " + tripCode);

        List<String> docNums = extractDocNums(trip);
        if (docNums.isEmpty())
            throw new RuntimeException("Trip has no documents to load: " + tripCode);

        Map<String, Object> resp = x3SoapService.validateStockLoad(docNums);
        if (resp != null && resp.get("error") != null) {
            throw new RuntimeException("X3 load truck failed: " + resp.get("error"));
        }

        List<Map<String, Object>> rows = resp != null ? (List<Map<String, Object>>) resp.getOrDefault("grp1", List.of()) : List.of();
        long failed = rows.stream().filter(r -> !isX3Success(r.get("o_xstatus"), r.get("o_xmsg"))).count();

        if (!rows.isEmpty() && failed > 0) {
            log.warn("Load truck partial failure for {} — {}/{} document(s) failed", tripCode, failed, rows.size());
            throw new RuntimeException("X3 load truck failed for " + failed + "/" + rows.size() + " document(s)");
        }

        lvs.setLoadFlag(1);
        lvs.setUpdatedAt(LocalDateTime.now());
        lvsHeaderRepository.save(lvs);
        log.info("TRUCK LOADED for {} (LVS {}) — {} document(s)", tripCode, lvs.getLvsNumber(), rows.size());

        return resp;
    }

    // A document/LVS action counts as successful if X3 returns its
    // normal success status (2) — OR if the message indicates it was
    // already done (already confirmed/created/loaded/exists), which X3
    // also reports as status 2 in every case we've seen (e.g.
    // XX10CVTLOC's "Location already created for this vehicle/trailer"),
    // but this message-based fallback covers it even if a specific
    // service ever returns a different status for that case.
    private boolean isX3Success(Object status, Object message) {
        if ("2".equals(String.valueOf(status))) return true;
        String msg = message != null ? String.valueOf(message).toLowerCase() : "";
        return msg.contains("already");
    }


    @Transactional
    public void lockTrips(List<String> tripCodes, String userCode) {
        tripCodes.forEach(c -> { try { lockTrip(c, userCode); } catch (Exception e) { log.error("lock {}: {}", c, e.getMessage()); }});
    }

    @Transactional
    public void validateTrips(List<String> tripCodes, String userCode) {
        tripCodes.forEach(c -> { try { validateTrip(c, userCode); } catch (Exception e) { log.error("validate {}: {}", c, e.getMessage()); }});
    }

    @Transactional
    public void unlockTrips(List<String> tripCodes, String userCode) {
        tripCodes.forEach(c -> { try { unlockTrip(c, userCode); } catch (Exception e) { log.error("unlock {}: {}", c, e.getMessage()); }});
    }

    // ═══════════════════════════════════════════════════════════
    // xr_vrheader — replaces XX10CPLANCHA
    // ═══════════════════════════════════════════════════════════
    private void writeVrHeader(XrTrip trip, String userCode) {
        LocalDateTime now = LocalDateTime.now();
        String usr = normalizeUser(userCode);

        VrHeader h = vrHeaderRepository.findById(trip.getTripCode()).orElseGet(VrHeader::new);
        boolean isNew = h.getCreatedAt() == null;

        h.setTripCode(trip.getTripCode());
        h.setVehicleCode(trip.getVehicleCode());
        h.setDriverId(trip.getDriverId());
        h.setSite(trip.getSite());
        h.setArrSite(trip.getArrSite());
        h.setStartTime(trip.getStartTime());
        h.setEndTime(trip.getEndTime());
        h.setDocDate(trip.getDocDate() != null ? trip.getDocDate() : LocalDate.now());
        h.setTotalDistance(parseDoubleSafe(trip.getTotalDistance()));
        h.setTotalTime(parseDoubleSafe(trip.getTotalTime()));
        h.setTotalCost(trip.getTotalCost());
        h.setTravelTime(trip.getTravelTime());
        h.setTotalCases(computeTotalCases(trip));
        h.setStatus(1);
        if (isNew) { h.setCreatedAt(now); h.setCreatedBy(usr); }
        h.setUpdatedAt(now);
        h.setUpdatedBy(usr);

        vrHeaderRepository.save(h);
        log.info("xr_vrheader written for {}", trip.getTripCode());
    }

    private double parseDoubleSafe(String val) {
        try { return val != null && !val.isBlank() ? Double.parseDouble(val) : 0.0; }
        catch (Exception e) { return 0.0; }
    }

    // ═══════════════════════════════════════════════════════════
    // xr_vrdetails — replaces XX10CPLANCHD (one row per stop)
    // ═══════════════════════════════════════════════════════════
    @SuppressWarnings("unchecked")
    private void writeVrDetails(XrTrip trip, String userCode) {
        vrDetailRepository.deleteByTripCode(trip.getTripCode());

        if (trip.getStopObjectsJson() == null || trip.getStopObjectsJson().isBlank()) return;

        List<Map<String, Object>> stops;
        try {
            stops = objectMapper.readValue(trip.getStopObjectsJson(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
        } catch (Exception e) { log.error("Cannot parse stops for {}: {}", trip.getTripCode(), e.getMessage()); return; }

        LocalDateTime now = LocalDateTime.now();
        int seq = 1;
        for (Map<String, Object> s : stops) {
            try {
                String docNum     = getString(s, "txn", "docNum", "id");
                String arrDate    = getString(s, "arrivalDate");
                String arrTime    = getString(s, "arrivalTime");
                String depDate    = getString(s, "departureDate");
                String depTime    = getString(s, "departureTime");
                String srvTime    = getString(s, "serviceTime");
                double waitNum    = parseDouble(getString(s, "waitingTime"));
                double prevDist   = parseDouble(getString(s, "fromPrevDistance"));
                double prevTravel = parseDouble(getString(s, "fromPrevTravelTime"));

                VrDetail d = new VrDetail();
                d.setTripCode(trip.getTripCode());
                d.setDocNum(docNum);
                d.setLineNum(seq * 1000);
                d.setSeq(seq);
                d.setFromPrevDistance(prevDist);
                d.setFromPrevTravelTime(prevTravel);
                d.setArrivalDate(parseDateTime(arrDate, arrTime));
                d.setArrivalTime(arrTime);
                d.setDepartureDate(parseDateTime(depDate, depTime));
                d.setDepartureTime(depTime);
                d.setServiceTime(srvTime);
                d.setWaitingTime(waitNum);
                // DROP=1, PICKUP=2 / X3 doc type 1=Delivery, 4=Pick Ticket —
                // same docType-based check as before (stopType is always
                // "DROP" for both, docType reliably distinguishes them).
                d.setPickupDrop(isPickTicket(s) ? 2 : 1);
                d.setDocTypeCode(isPickTicket(s) ? 4 : 1);
                d.setSite(trip.getSite());
                d.setCreatedAt(now);
                d.setUpdatedAt(now);

                vrDetailRepository.save(d);
                seq++;
            } catch (Exception e) {
                log.error("xr_vrdetails stop {} for {}: {}", seq, trip.getTripCode(), e.getMessage());
            }
        }
        log.info("xr_vrdetails: {} rows written for {}", stops.size(), trip.getTripCode());
    }

    // ═══════════════════════════════════════════════════════════
    // XX10CDOCUP — one row per document/stop, sent to X3 on Lock
    // ═══════════════════════════════════════════════════════════
    @SuppressWarnings("unchecked")
    private void updateDocumentsInX3(XrTrip trip) {
        if (trip.getStopObjectsJson() == null || trip.getStopObjectsJson().isBlank()) return;

        List<Map<String, Object>> stops;
        try {
            stops = objectMapper.readValue(trip.getStopObjectsJson(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
        } catch (Exception e) {
            log.error("Cannot parse stops for XX10CDOCUP {}: {}", trip.getTripCode(), e.getMessage());
            return;
        }
        if (stops.isEmpty()) return;

        // Trailer comes from the vehicle assigned to this trip, not the
        // trip itself — XrTrip has no trailer field of its own.
        String trailer = "";
        if (trip.getVehicleCode() != null) {
            Vehicle v = vehicleRepository.findById(trip.getVehicleCode()).orElse(null);
            if (v != null && v.getTrailerNumber() != null) trailer = v.getTrailerNumber();
        }

        LocalDate docDate = trip.getDocDate() != null ? trip.getDocDate() : LocalDate.now();
        String trDate = docDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        List<Map<String, String>> rows = new ArrayList<>();
        int seq = 1;
        for (Map<String, Object> s : stops) {
            String docNum = getString(s, "txn", "docNum", "id");
            if (docNum == null) { seq++; continue; }

            Map<String, String> row = new LinkedHashMap<>();
            row.put("docNum", docNum);
            row.put("vehNum", trip.getVehicleCode());
            row.put("driverId", trip.getDriverId());
            row.put("seq", String.valueOf(seq));
            row.put("status", "1");
            row.put("trailer", trailer);
            row.put("trDate", trDate);
            rows.add(row);
            seq++;
        }
        if (rows.isEmpty()) return;

        Map<String, Object> resp = x3SoapService.updateDocuments(rows);
        if (resp != null && resp.get("error") != null) {
            log.error("XX10CDOCUP error for {}: {}", trip.getTripCode(), resp.get("error"));
        } else {
            log.info("XX10CDOCUP: {} document(s) updated in X3 for {}", rows.size(), trip.getTripCode());
        }
    }

    private LocalDateTime parseDateTime(String date, String time) {
        try {
            if (date == null || date.isBlank()) return LocalDateTime.now();
            String d = date.trim();
            String t = (time != null && !time.isBlank()) ? time.trim().substring(0, 5) : "00:00";
            return LocalDateTime.parse(d + "T" + t + ":00");
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    private double parseDouble(String val) {
        try { return val != null ? Double.parseDouble(val) : 0.0; }
        catch (Exception e) { return 0.0; }
    }

    // ═══════════════════════════════════════════════════════════
    // xr_lvsheader — replaces XX10CLODSTOH
    // ═══════════════════════════════════════════════════════════
    private void writeLvsHeader(XrTrip trip, String userCode) {
        if (lvsHeaderRepository.findByTripCode(trip.getTripCode()).isPresent()) {
            log.info("LVS already exists for {}", trip.getTripCode());
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        String usr = normalizeUser(userCode);
        LocalDate doc = trip.getDocDate() != null ? trip.getDocDate() : LocalDate.now();

        String lvsNum = generateLvsNumber(trip.getSite(), doc);

        LvsHeader h = new LvsHeader();
        h.setLvsNumber(lvsNum);
        h.setTripCode(trip.getTripCode());
        h.setSite(trip.getSite());
        h.setArrSite(trip.getArrSite());
        h.setVehicleCode(trip.getVehicleCode());
        h.setDriverId(trip.getDriverId());
        h.setDocDate(doc);
        h.setDepartureDate(doc);
        h.setDepartureTime(trip.getStartTime());
        h.setArrivalDate(doc);
        h.setArrivalTime(trip.getEndTime());
        h.setCapacityWeight(parseDoubleSafe(trip.getTotalWeight()));
        h.setTotalCases(computeTotalCases(trip));
        h.setConfirmedFlag(0);
        h.setLoadFlag(0);
        h.setCreatedAt(now);
        h.setCreatedBy(usr);
        h.setUpdatedAt(now);
        h.setUpdatedBy(usr);

        lvsHeaderRepository.save(h);
        log.info("xr_lvsheader written for trip {} -> {}", trip.getTripCode(), lvsNum);
    }

    // ═══════════════════════════════════════════════════════════
    // Update doc status on VALIDATE — still X3's own delivery documents
    // ═══════════════════════════════════════════════════════════
    @SuppressWarnings("unchecked")
    private void updateDocStatusOnValidate(XrTrip trip, String x3) {
        if (trip.getStopObjectsJson() == null) return;
        try {
            List<Map<String, Object>> stops = objectMapper.readValue(trip.getStopObjectsJson(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
            for (Map<String, Object> s : stops) {
                String docNum = getString(s, "txn", "docNum", "id");
                if (docNum == null) continue;
                if (isPickTicket(s)) {
                    sqlServerJdbc.update("UPDATE " + x3 + ".STOPREH SET XDLV_STATUS_0 = 2 WHERE PRHNUM_0 = ?", docNum);
                } else {
                    sqlServerJdbc.update("UPDATE " + x3 + ".SDELIVERY SET XDLV_STATUS_0 = 2 WHERE SDHNUM_0 = ?", docNum);
                }
            }
        } catch (Exception e) { log.warn("Validate doc status failed for {}: {}", trip.getTripCode(), e.getMessage()); }
    }

    // Sums each stop's package/case count (qty, sourced from nbPack on the
    // stops view) to give total_cases a real value instead of a hardcoded 0.
    @SuppressWarnings("unchecked")
    private int computeTotalCases(XrTrip trip) {
        if (trip.getStopObjectsJson() == null) return 0;
        try {
            List<Map<String, Object>> stops = objectMapper.readValue(trip.getStopObjectsJson(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
            int total = 0;
            for (Map<String, Object> s : stops) {
                String raw = getString(s, "qty", "nbPack");
                if (raw == null || raw.isBlank()) continue;
                try { total += (int) Double.parseDouble(raw); }
                catch (NumberFormatException ignored) { /* skip unparsable */ }
            }
            return total;
        } catch (Exception e) {
            log.warn("computeTotalCases failed for {}: {}", trip.getTripCode(), e.getMessage());
            return 0;
        }
    }

    // ── Helpers ───────────────────────────────────────────────
    private boolean isPickTicket(Map<String, Object> stop) {
        String docType = getString(stop, "docType", "doctype");
        if (docType != null) return "PICK".equalsIgnoreCase(docType);
        return "PICKUP".equals(getString(stop, "type", "stopType"));
    }

    private String normalizeUser(String userCode) {
        return userCode != null && userCode.length() > 5 ? userCode.substring(0, 5) : (userCode != null ? userCode : "SYS");
    }

    // ── Generate LVS number: {SITE}{YY}{MM}XCHG{0000001} ──────
    // Now sequenced against xr_lvsheader (Postgres) instead of querying
    // X3's XX10CLODSTOH over SQL Server.
    private String generateLvsNumber(String site, LocalDate docDate) {
        String s = site != null ? site : "TMS";
        String yy = String.format("%02d", (docDate != null ? docDate.getYear() : LocalDate.now().getYear()) % 100);
        String mm = String.format("%02d", docDate != null ? docDate.getMonthValue() : LocalDate.now().getMonthValue());
        String prefix = s + yy + mm + "XCHG";

        int nextSeq = 1;
        List<LvsHeader> existing = lvsHeaderRepository.findByLvsNumberStartingWithOrderByLvsNumberDesc(prefix);
        if (!existing.isEmpty()) {
            String maxVal = existing.get(0).getLvsNumber();
            if (maxVal.length() > prefix.length()) {
                try { nextSeq = Integer.parseInt(maxVal.substring(prefix.length())) + 1; }
                catch (Exception ignored) {}
            }
        }
        return prefix + String.format("%07d", nextSeq);
    }

    private XrTrip findTrip(String tripCode) {
        return tripRepository.findByTripCode(tripCode)
            .orElseThrow(() -> new RuntimeException("Trip not found: " + tripCode));
    }

    private String getString(Map<String, Object> m, String... keys) {
        for (String k : keys) { Object v = m.get(k); if (v != null) return v.toString(); }
        return null;
    }

    /** Every document/stop number on a trip — shared by
     *  updateDocumentsInX3, confirmLvs, loadTruck, and unlockTrip, which
     *  all need the same "which documents are on this trip" list. */
    @SuppressWarnings("unchecked")
    private List<String> extractDocNums(XrTrip trip) {
        if (trip.getStopObjectsJson() == null || trip.getStopObjectsJson().isBlank()) return List.of();
        List<Map<String, Object>> stops;
        try {
            stops = objectMapper.readValue(trip.getStopObjectsJson(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
        } catch (Exception e) {
            log.error("Cannot parse stops for {}: {}", trip.getTripCode(), e.getMessage());
            return List.of();
        }
        List<String> docNums = new ArrayList<>();
        for (Map<String, Object> s : stops) {
            String docNum = getString(s, "txn", "docNum", "id");
            if (docNum != null) docNums.add(docNum);
        }
        return docNums;
    }
}
