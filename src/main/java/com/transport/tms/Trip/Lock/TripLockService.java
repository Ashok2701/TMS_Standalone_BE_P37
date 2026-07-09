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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Handles LOCK and VALIDATE actions — writes to X3 SQL Server tables.
 *
 * LOCK   → XX10CPLANCHA (header) + XX10CPLANCHD (stops detail)
 * VALIDATE → XX10CLODSTOH (LVS header)
 * UNLOCK → DELETE from XX10CPLANCHA + XX10CPLANCHD
 *
 * Column names match exactly what the old CBTTL system uses.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TripLockService {

    private final TripRepository tripRepository;
    private final SchemaConfig   schemas;
    private final ObjectMapper   objectMapper;

    @Qualifier("sqlServerJdbcTemplate")
    private final JdbcTemplate   sqlServerJdbc;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ── LOCK ─────────────────────────────────────────────────
    @Transactional
    public void lockTrip(Long tripId, String userCode) {
        XrTrip trip = findTrip(tripId);
        if (trip.getLockFlag() != null && trip.getLockFlag() == 1)
            throw new RuntimeException("Trip already locked: " + trip.getTripCode());

        String x3 = schemas.getX3Schema();
        writePlanningHeader(trip, x3, userCode);
        writePlanningDetails(trip, x3, userCode);

        // Update XX10TRIPS: lock=1, optistatus=Locked
        try { sqlServerJdbc.update("UPDATE " + x3 + ".XX10TRIPS SET lock = 1, optistatus = ? WHERE TRIPCODE = ?", "Locked", trip.getTripCode()); }
        catch (Exception e) { log.warn("XX10TRIPS lock update failed: {}", e.getMessage()); }

        trip.setOptiStatus("Locked");
        trip.setLockFlag(1);
        trip.setDatExec(OffsetDateTime.now());
        tripRepository.save(trip);
        log.info("LOCKED {} → {}.XX10CPLANCHA + XX10CPLANCHD", trip.getTripCode(), x3);
    }

    // ── VALIDATE ──────────────────────────────────────────────
    @Transactional
    public void validateTrip(Long tripId, String userCode) {
        XrTrip trip = findTrip(tripId);
        if (trip.getLockFlag() == null || trip.getLockFlag() == 0)
            throw new RuntimeException("Trip must be locked before validation: " + trip.getTripCode());

        String x3 = schemas.getX3Schema();
        writeLVSHeader(trip, x3, userCode);

        trip.setOptiStatus("Validated");
        tripRepository.save(trip);
        log.info("VALIDATED {} → {}.XX10CLODSTOH", trip.getTripCode(), x3);
    }

    // ── UNLOCK ────────────────────────────────────────────────
    @Transactional
    public void unlockTrip(Long tripId, String userCode) {
        XrTrip trip = findTrip(tripId);
        if ("Validated".equals(trip.getOptiStatus()))
            throw new RuntimeException("Validated trips cannot be unlocked: " + trip.getTripCode());

        String x3  = schemas.getX3Schema();
        String code = trip.getTripCode();
        int d = sqlServerJdbc.update("DELETE FROM " + x3 + ".XX10CPLANCHD WHERE XNUMPC_0 = ?", code);
        int h = sqlServerJdbc.update("DELETE FROM " + x3 + ".XX10CPLANCHA WHERE XNUMPC_0 = ?", code);

        // Reset XX10TRIPS.lock = 0
        try {
            sqlServerJdbc.update(
                "UPDATE " + x3 + ".XX10TRIPS SET lock = 0, optistatus = 'Open' WHERE TRIPCODE = ?",
                code
            );
        } catch (Exception e) {
            log.warn("XX10TRIPS unlock update failed for {}: {}", code, e.getMessage());
        }

        trip.setOptiStatus("Optimised");
        trip.setLockFlag(0);
        tripRepository.save(trip);
        log.info("UNLOCKED {} — deleted {}H {}D rows", code, h, d);
    }

    // ── GROUP actions ─────────────────────────────────────────
    @Transactional
    public void lockTrips(List<Long> ids, String userCode) {
        ids.forEach(id -> { try { lockTrip(id, userCode); } catch(Exception e) { log.error("lock {}: {}", id, e.getMessage()); }});
    }

    @Transactional
    public void validateTrips(List<Long> ids, String userCode) {
        ids.forEach(id -> { try { validateTrip(id, userCode); } catch(Exception e) { log.error("validate {}: {}", id, e.getMessage()); }});
    }

    // ═══════════════════════════════════════════════════════════
    // XX10CPLANCHA — Planning header (same columns as old CBTTL)
    // ═══════════════════════════════════════════════════════════
    private void writePlanningHeader(XrTrip trip, String x3, String userCode) {
        // Upsert: delete first
        sqlServerJdbc.update("DELETE FROM " + x3 + ".XX10CPLANCHA WHERE XNUMPC_0 = ?", trip.getTripCode());

        // Derive heuexec (execution time — current time)
        String heuexec = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        String sql = "INSERT INTO " + x3 + ".XX10CPLANCHA ("
            + "XNUMPC_0, XFCY_0, XDATLIV_0, XVEHCODE_0, XDRIVERID_0, "
            + "XDESFCY_0, XHEUDEP_0, XHEUARR_0, XHEUEXEC_0, "
            + "XTOTDIST_0, XTOTTIME_0, XTRVTIME_0, XSRVTIME_0, "
            + "XDROPS_0, XPICKUPS_0, XSTOPS_0, XNOPACK_0, "
            + "XTOTWEIGHT_0, XTOTVOL_0, XCAPACITY_0, "
            + "XOPTISTATUS_0, XDEPSIT_0, XARRSIT_0, "
            + "XGENBY_0, XUSRCODE_0, XCREDATTIM_0"
            + ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        sqlServerJdbc.update(sql,
            trip.getTripCode(),              // XNUMPC_0
            trip.getSite(),                  // XFCY_0
            trip.getDocDate(),               // XDATLIV_0
            trip.getVehicleCode(),           // XVEHCODE_0
            trip.getDriverId(),              // XDRIVERID_0
            trip.getArrSite(),               // XDESFCY_0  (destination site)
            trip.getStartTime(),             // XHEUDEP_0  (scheduled departure time)
            trip.getEndTime(),               // XHEUARR_0  (scheduled return time)
            heuexec,                         // XHEUEXEC_0 (execution time)
            trip.getTotalDistance(),         // XTOTDIST_0
            trip.getTotalTime(),             // XTOTTIME_0
            trip.getTravelTime(),            // XTRVTIME_0
            trip.getServiceTime(),           // XSRVTIME_0
            trip.getDrops(),                 // XDROPS_0
            trip.getPickups(),               // XPICKUPS_0
            stops(trip),                     // XSTOPS_0
            trip.getNoOfPackages(),          // XNOPACK_0
            trip.getTotalWeight(),           // XTOTWEIGHT_0
            trip.getTotalVolume(),           // XTOTVOL_0
            trip.getCapacity(),              // XCAPACITY_0
            "Locked",                        // XOPTISTATUS_0
            trip.getDepSite(),               // XDEPSIT_0
            trip.getArrSite(),               // XARRSIT_0
            trip.getGeneratedBy(),           // XGENBY_0
            userCode,                        // XUSRCODE_0
            LocalDateTime.now()              // XCREDATTIM_0
        );
    }

    // ═══════════════════════════════════════════════════════════
    // XX10CPLANCHD — Planning detail per stop
    // ═══════════════════════════════════════════════════════════
    @SuppressWarnings("unchecked")
    private void writePlanningDetails(XrTrip trip, String x3, String userCode) {
        sqlServerJdbc.update("DELETE FROM " + x3 + ".XX10CPLANCHD WHERE XNUMPC_0 = ?", trip.getTripCode());

        if (trip.getStopObjectsJson() == null || trip.getStopObjectsJson().isBlank()) return;

        List<Map<String, Object>> stops;
        try {
            stops = objectMapper.readValue(trip.getStopObjectsJson(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
        } catch (Exception e) {
            log.error("Cannot parse stopObjects for {}: {}", trip.getTripCode(), e.getMessage());
            return;
        }

        String sql = "INSERT INTO " + x3 + ".XX10CPLANCHD ("
            + "XNUMPC_0, SDHNUM_0, XSEQ_0, XTYPE_0, "
            + "XBPCODE_0, XADRESCODE_0, XCITY_0, "
            + "XARRDATE_0, XARRTIME_0, XDEPDATE_0, XDEPTIME_0, "
            + "XSRVTIME_0, XWAITTIME_0, "
            + "XDISTFRMPREV_0, XTRVTIMEFRMPREV_0, "
            + "XNETWEIGHT_0, XVOLUME_0, "
            + "XUSRCODE_0, XCREDATTIM_0"
            + ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        int seq = 1;
        for (Map<String, Object> s : stops) {
            try {
                sqlServerJdbc.update(sql,
                    trip.getTripCode(),                              // XNUMPC_0
                    str(s, "txn","docNum","id"),                    // SDHNUM_0  — document number
                    seq++,                                           // XSEQ_0    — sequence
                    str(s, "type","stopType"),                      // XTYPE_0   — DROP|PICKUP
                    str(s, "bpcode","bpCode"),                      // XBPCODE_0 — customer code
                    str(s, "addressCode","adrescode"),              // XADRESCODE_0
                    str(s, "city"),                                  // XCITY_0
                    str(s, "arrivalDate"),                           // XARRDATE_0
                    str(s, "arrivalTime"),                           // XARRTIME_0
                    str(s, "departureDate"),                         // XDEPDATE_0
                    str(s, "departureTime"),                         // XDEPTIME_0
                    str(s, "serviceTime"),                           // XSRVTIME_0
                    str(s, "waitingTime"),                           // XWAITTIME_0
                    str(s, "fromPrevDistance"),                      // XDISTFRMPREV_0
                    str(s, "fromPrevTravelTime"),                    // XTRVTIMEFRMPREV_0
                    dbl(s, "netweight","netWeight"),                 // XNETWEIGHT_0
                    dbl(s, "vol","volume"),                          // XVOLUME_0
                    userCode,                                        // XUSRCODE_0
                    LocalDateTime.now()                              // XCREDATTIM_0
                );
            } catch (Exception e) {
                log.error("Stop {} for trip {}: {}", seq, trip.getTripCode(), e.getMessage());
            }
        }
        log.info("XX10CPLANCHD: {} rows for {}", stops.size(), trip.getTripCode());
    }

    // ═══════════════════════════════════════════════════════════
    // XX10CLODSTOH — Load Vehicle Stock header (LVS)
    // ═══════════════════════════════════════════════════════════
    private void writeLVSHeader(XrTrip trip, String x3, String userCode) {
        Integer cnt = sqlServerJdbc.queryForObject(
            "SELECT COUNT(*) FROM " + x3 + ".XX10CLODSTOH WHERE XVRSEL_0 = ?",
            Integer.class, trip.getTripCode()
        );
        if (cnt != null && cnt > 0) {
            log.info("XX10CLODSTOH already exists for {} — skipping", trip.getTripCode());
            return;
        }

        String sql = "INSERT INTO " + x3 + ".XX10CLODSTOH ("
            + "XVRSEL_0, XFCY_0, XDATLIV_0, XVEHCODE_0, XDRIVERID_0, "
            + "XHEUDEP_0, XHEUARR_0, "
            + "XTOTDIST_0, XTOTTIME_0, XTRVTIME_0, "
            + "XDROPS_0, XPICKUPS_0, XNOPACK_0, "
            + "XTOTWEIGHT_0, XTOTVOL_0, "
            + "XLOADFLG_0, XDEPSIT_0, XARRSIT_0, "
            + "XUSRCODE_0, XCREDATTIM_0"
            + ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        sqlServerJdbc.update(sql,
            trip.getTripCode(),          // XVRSEL_0   — trip/VR code (FK link back to PLANCHA)
            trip.getSite(),              // XFCY_0
            trip.getDocDate(),           // XDATLIV_0
            trip.getVehicleCode(),       // XVEHCODE_0
            trip.getDriverId(),          // XDRIVERID_0
            trip.getStartTime(),         // XHEUDEP_0
            trip.getEndTime(),           // XHEUARR_0
            trip.getTotalDistance(),     // XTOTDIST_0
            trip.getTotalTime(),         // XTOTTIME_0
            trip.getTravelTime(),        // XTRVTIME_0
            trip.getDrops(),             // XDROPS_0
            trip.getPickups(),           // XPICKUPS_0
            trip.getNoOfPackages(),      // XNOPACK_0
            trip.getTotalWeight(),       // XTOTWEIGHT_0
            trip.getTotalVolume(),       // XTOTVOL_0
            1,                           // XLOADFLG_0 = 1 = LVS Generated
            trip.getDepSite(),           // XDEPSIT_0
            trip.getArrSite(),           // XARRSIT_0
            userCode,                    // XUSRCODE_0
            LocalDateTime.now()          // XCREDATTIM_0
        );
        log.info("XX10CLODSTOH written for {}", trip.getTripCode());
    }

    // ── Update doc status on VALIDATE ────────────────────────
    @SuppressWarnings("unchecked")
    private void updateDocStatusOnValidate(XrTrip trip, String x3) {
        if (trip.getStopObjectsJson() == null || trip.getStopObjectsJson().isBlank()) return;
        try {
            List<Map<String, Object>> stops = objectMapper.readValue(
                trip.getStopObjectsJson(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class)
            );
            for (Map<String, Object> stop : stops) {
                String docNum = getString(stop, "txn", "docNum", "id");
                String type   = getString(stop, "type", "stopType");
                if (docNum == null) continue;

                if ("PICKUP".equals(type)) {
                    sqlServerJdbc.update(
                        "UPDATE " + x3 + ".STOPREH SET XDLV_STATUS_0 = 2 WHERE PRHNUM_0 = ?",
                        docNum
                    );
                } else {
                    sqlServerJdbc.update(
                        "UPDATE " + x3 + ".SDELIVERY SET XDLV_STATUS_0 = 2 WHERE SDHNUM_0 = ?",
                        docNum
                    );
                }
            }
            log.info("VALIDATE: XDLV_STATUS_0 = 2 set for {} stops of trip {}",
                stops.size(), trip.getTripCode());
        } catch (Exception e) {
            log.warn("Validate doc status update failed for {}: {}", trip.getTripCode(), e.getMessage());
        }
    }

    // ── Helpers ───────────────────────────────────────────────
    private String getString(Map<String, Object> m, String... keys) {
        for (String k : keys) {
            Object v = m.get(k);
            if (v != null) return v.toString();
        }
        return null;
    }

    private XrTrip findTrip(Long id) {
        return tripRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Trip not found: " + id));
    }

    private int stops(XrTrip t) {
        int d = t.getDrops()   != null ? t.getDrops()   : 0;
        int p = t.getPickups() != null ? t.getPickups() : 0;
        return d + p;
    }

    private String str(Map<String, Object> m, String... keys) {
        for (String k : keys) {
            Object v = m.get(k);
            if (v != null) return v.toString();
        }
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
