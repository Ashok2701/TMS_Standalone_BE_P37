package com.transport.tms.Trip.Lock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transport.tms.Config.SchemaConfig;
import com.transport.tms.Trip.Entity.XrTrip;
import com.transport.tms.Trip.Repository.TripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TripLockService {

    private final TripRepository tripRepository;
    private final SchemaConfig   schemas;
    private final ObjectMapper   objectMapper;

    @Qualifier("sqlServerJdbcTemplate")
    private final JdbcTemplate   sqlServerJdbc;

    // ── LOCK ─────────────────────────────────────────────────
    @Transactional
    public void lockTrip(String tripCode, String userCode) {
        XrTrip trip = findTrip(tripCode);
        if (trip.getLockFlag() != null && trip.getLockFlag() == 1)
            throw new RuntimeException("Trip already locked: " + tripCode);

        String x3 = schemas.getX3Schema();

        // 1. XX10CPLANCHA + XX10CPLANCHD
        writePlanningHeader(trip, x3, userCode);
        writePlanningDetails(trip, x3, userCode);

        // 2. XX10TRIPS
        try { sqlServerJdbc.update(
            "UPDATE " + x3 + ".XX10TRIPS SET lock = 1, optistatus = ? WHERE TRIPCODE = ?",
            "Locked", tripCode);
        } catch (Exception e) { log.warn("XX10TRIPS lock failed: {}", e.getMessage()); }

        // 3. Postgres
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

        // 1. XX10CLODSTOH
        writeLVSHeader(trip, x3, userCode);

        // 2. SDELIVERY + STOPREH XDLV_STATUS_0 = 2
        updateDocStatusOnValidate(trip, x3);

        // 3. Postgres
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

        String x3 = schemas.getX3Schema();

        // 1. Delete planning tables
        sqlServerJdbc.update("DELETE FROM " + x3 + ".XX10CPLANCHD WHERE XNUMPC_0 = ?", tripCode);
        sqlServerJdbc.update("DELETE FROM " + x3 + ".XX10CPLANCHA WHERE XNUMPC_0 = ?", tripCode);

        // 2. Reset XX10TRIPS
        try { sqlServerJdbc.update(
            "UPDATE " + x3 + ".XX10TRIPS SET lock = 0, optistatus = ? WHERE TRIPCODE = ?",
            "Open", tripCode);
        } catch (Exception e) { log.warn("XX10TRIPS unlock failed: {}", e.getMessage()); }

        // 3. Postgres
        trip.setOptiStatus("Optimised");
        trip.setLockFlag(0);
        tripRepository.save(trip);
        log.info("UNLOCKED {}", tripCode);
    }

    // ── GROUP ─────────────────────────────────────────────────
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
    // XX10CPLANCHA
    // ═══════════════════════════════════════════════════════════
    private void writePlanningHeader(XrTrip trip, String x3, String userCode) {
        sqlServerJdbc.update("DELETE FROM " + x3 + ".XX10CPLANCHA WHERE XNUMPC_0 = ?", trip.getTripCode());

        String heuexec = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));

        sqlServerJdbc.update(
            "INSERT INTO " + x3 + ".XX10CPLANCHA ("
            + "XNUMPC_0,XFCY_0,XDATLIV_0,XVEHCODE_0,XDRIVERID_0,"
            + "XDESFCY_0,XHEUDEP_0,XHEUARR_0,XHEUEXEC_0,"
            + "XTOTDIST_0,XTOTTIME_0,XTRVTIME_0,XSRVTIME_0,"
            + "XDROPS_0,XPICKUPS_0,XSTOPS_0,XNOPACK_0,"
            + "XTOTWEIGHT_0,XTOTVOL_0,XCAPACITY_0,"
            + "XOPTISTATUS_0,XDEPSIT_0,XARRSIT_0,"
            + "XGENBY_0,XUSRCODE_0,XCREDATTIM_0"
            + ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
            trip.getTripCode(), trip.getSite(), trip.getDocDate(),
            trip.getVehicleCode(), trip.getDriverId(), trip.getArrSite(),
            trip.getStartTime(), trip.getEndTime(), heuexec,
            trip.getTotalDistance(), trip.getTotalTime(), trip.getTravelTime(), trip.getServiceTime(),
            trip.getDrops(), trip.getPickups(),
            (trip.getDrops() != null ? trip.getDrops() : 0) + (trip.getPickups() != null ? trip.getPickups() : 0),
            trip.getNoOfPackages(),
            trip.getTotalWeight(), trip.getTotalVolume(), trip.getCapacity(),
            "Locked", trip.getDepSite(), trip.getArrSite(),
            trip.getGeneratedBy(), userCode, LocalDateTime.now()
        );
    }

    // ═══════════════════════════════════════════════════════════
    // XX10CPLANCHD
    // ═══════════════════════════════════════════════════════════
    @SuppressWarnings("unchecked")
    private void writePlanningDetails(XrTrip trip, String x3, String userCode) {
        sqlServerJdbc.update("DELETE FROM " + x3 + ".XX10CPLANCHD WHERE XNUMPC_0 = ?", trip.getTripCode());

        if (trip.getStopObjectsJson() == null || trip.getStopObjectsJson().isBlank()) return;

        List<Map<String, Object>> stops;
        try {
            stops = objectMapper.readValue(trip.getStopObjectsJson(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
        } catch (Exception e) { log.error("Cannot parse stops for {}: {}", trip.getTripCode(), e.getMessage()); return; }

        String sql = "INSERT INTO " + x3 + ".XX10CPLANCHD ("
            + "XNUMPC_0,SDHNUM_0,XSEQ_0,XTYPE_0,"
            + "XBPCODE_0,XADRESCODE_0,XCITY_0,"
            + "XARRDATE_0,XARRTIME_0,XDEPDATE_0,XDEPTIME_0,"
            + "XSRVTIME_0,XWAITTIME_0,"
            + "XDISTFRMPREV_0,XTRVTIMEFRMPREV_0,"
            + "XNETWEIGHT_0,XVOLUME_0,XUSRCODE_0,XCREDATTIM_0"
            + ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        int seq = 1;
        for (Map<String, Object> s : stops) {
            try {
                sqlServerJdbc.update(sql,
                    trip.getTripCode(), getString(s,"txn","docNum","id"), seq++,
                    getString(s,"type","stopType"),
                    getString(s,"bpcode","bpCode"), getString(s,"addressCode","adrescode"),
                    getString(s,"city"),
                    getString(s,"arrivalDate"), getString(s,"arrivalTime"),
                    getString(s,"departureDate"), getString(s,"departureTime"),
                    getString(s,"serviceTime"), getString(s,"waitingTime"),
                    getString(s,"fromPrevDistance"), getString(s,"fromPrevTravelTime"),
                    dbl(s,"netweight","netWeight"), dbl(s,"vol","volume"),
                    userCode, LocalDateTime.now()
                );
            } catch (Exception e) { log.error("Stop {} for {}: {}", seq, trip.getTripCode(), e.getMessage()); }
        }
        log.info("XX10CPLANCHD: {} rows for {}", stops.size(), trip.getTripCode());
    }

    // ═══════════════════════════════════════════════════════════
    // XX10CLODSTOH
    // ═══════════════════════════════════════════════════════════
    private void writeLVSHeader(XrTrip trip, String x3, String userCode) {
        Integer cnt = sqlServerJdbc.queryForObject(
            "SELECT COUNT(*) FROM " + x3 + ".XX10CLODSTOH WHERE XVRSEL_0 = ?",
            Integer.class, trip.getTripCode());
        if (cnt != null && cnt > 0) { log.info("LVS already exists for {}", trip.getTripCode()); return; }

        sqlServerJdbc.update(
            "INSERT INTO " + x3 + ".XX10CLODSTOH ("
            + "XVRSEL_0,XFCY_0,XDATLIV_0,XVEHCODE_0,XDRIVERID_0,"
            + "XHEUDEP_0,XHEUARR_0,XTOTDIST_0,XTOTTIME_0,XTRVTIME_0,"
            + "XDROPS_0,XPICKUPS_0,XNOPACK_0,XTOTWEIGHT_0,XTOTVOL_0,"
            + "XLOADFLG_0,XDEPSIT_0,XARRSIT_0,XUSRCODE_0,XCREDATTIM_0"
            + ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
            trip.getTripCode(), trip.getSite(), trip.getDocDate(),
            trip.getVehicleCode(), trip.getDriverId(),
            trip.getStartTime(), trip.getEndTime(),
            trip.getTotalDistance(), trip.getTotalTime(), trip.getTravelTime(),
            trip.getDrops(), trip.getPickups(), trip.getNoOfPackages(),
            trip.getTotalWeight(), trip.getTotalVolume(),
            1, trip.getDepSite(), trip.getArrSite(), userCode, LocalDateTime.now()
        );
        log.info("XX10CLODSTOH written for {}", trip.getTripCode());
    }

    // ═══════════════════════════════════════════════════════════
    // Update doc status on VALIDATE
    // ═══════════════════════════════════════════════════════════
    @SuppressWarnings("unchecked")
    private void updateDocStatusOnValidate(XrTrip trip, String x3) {
        if (trip.getStopObjectsJson() == null) return;
        try {
            List<Map<String, Object>> stops = objectMapper.readValue(trip.getStopObjectsJson(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
            for (Map<String, Object> s : stops) {
                String docNum = getString(s, "txn", "docNum", "id");
                String type   = getString(s, "type", "stopType");
                if (docNum == null) continue;
                if ("PICKUP".equals(type)) {
                    sqlServerJdbc.update("UPDATE " + x3 + ".STOPREH SET XDLV_STATUS_0 = 2 WHERE PRHNUM_0 = ?", docNum);
                } else {
                    sqlServerJdbc.update("UPDATE " + x3 + ".SDELIVERY SET XDLV_STATUS_0 = 2 WHERE SDHNUM_0 = ?", docNum);
                }
            }
        } catch (Exception e) { log.warn("Validate doc status failed for {}: {}", trip.getTripCode(), e.getMessage()); }
    }

    // ── Helpers ───────────────────────────────────────────────
    private XrTrip findTrip(String tripCode) {
        return tripRepository.findByTripCode(tripCode)
            .orElseThrow(() -> new RuntimeException("Trip not found: " + tripCode));
    }

    private String getString(Map<String, Object> m, String... keys) {
        for (String k : keys) { Object v = m.get(k); if (v != null) return v.toString(); }
        return null;
    }

    private Double dbl(Map<String, Object> m, String... keys) {
        for (String k : keys) {
            Object v = m.get(k);
            if (v instanceof Number) return ((Number) v).doubleValue();
            if (v instanceof String) { try { return Double.parseDouble((String) v); } catch (Exception ignored) {} }
        }
        return null;
    }
}
