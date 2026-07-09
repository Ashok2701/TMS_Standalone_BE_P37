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
import java.util.List;
import java.util.Map;

/**
 * Handles LOCK and VALIDATE actions for trips.
 *
 * LOCK:
 *   Writes trip header → XX10CPLANCHA (one row per trip)
 *   Writes stop lines  → XX10CPLANCHD (one row per stop)
 *   Sets xr_trip.opti_status = 'Locked', lock_flag = 1
 *
 * VALIDATE:
 *   Writes load vehicle stock → XX10CLODSTOH (one row per trip)
 *   Sets xr_trip.opti_status = 'Validated', lock_flag = 1
 *
 * UNLOCK:
 *   Deletes from XX10CPLANCHA + XX10CPLANCHD for this trip
 *   Sets xr_trip.opti_status = 'Optimised', lock_flag = 0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TripLockService {

    private final TripRepository            tripRepository;
    private final SchemaConfig              schemas;
    private final ObjectMapper              objectMapper;

    @Qualifier("sqlServerJdbcTemplate")
    private final JdbcTemplate              sqlServerJdbc;

    // ── LOCK a single trip ────────────────────────────────────
    @Transactional
    public void lockTrip(Long tripId, String userCode) {
        XrTrip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found: " + tripId));

        if (trip.getLockFlag() != null && trip.getLockFlag() == 1) {
            throw new RuntimeException("Trip is already locked: " + trip.getTripCode());
        }

        String x3 = schemas.getX3Schema();

        // 1. Write to XX10CPLANCHA (planning header)
        writePlanningHeader(trip, x3, userCode);

        // 2. Write to XX10CPLANCHD (planning detail — one row per stop)
        writePlanningDetails(trip, x3, userCode);

        // 3. Update Postgres trip status
        trip.setOptiStatus("Locked");
        trip.setLockFlag(1);
        trip.setDatExec(LocalDateTime.now());
        tripRepository.save(trip);

        log.info("Trip LOCKED: {} → XX10CPLANCHA + XX10CPLANCHD written", trip.getTripCode());
    }

    // ── VALIDATE a single trip ────────────────────────────────
    @Transactional
    public void validateTrip(Long tripId, String userCode) {
        XrTrip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found: " + tripId));

        if (trip.getLockFlag() == null || trip.getLockFlag() == 0) {
            throw new RuntimeException("Trip must be locked before validation: " + trip.getTripCode());
        }

        String x3 = schemas.getX3Schema();

        // Write to XX10CLODSTOH (Load Vehicle Stock header)
        writeLVSHeader(trip, x3, userCode);

        // Update Postgres trip status
        trip.setOptiStatus("Validated");
        tripRepository.save(trip);

        log.info("Trip VALIDATED: {} → XX10CLODSTOH written", trip.getTripCode());
    }

    // ── UNLOCK a trip ─────────────────────────────────────────
    @Transactional
    public void unlockTrip(Long tripId, String userCode) {
        XrTrip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found: " + tripId));

        if ("Validated".equals(trip.getOptiStatus())) {
            throw new RuntimeException("Validated trips cannot be unlocked: " + trip.getTripCode());
        }

        String x3 = schemas.getX3Schema();
        String tripCode = trip.getTripCode();

        // Delete from XX10CPLANCHD (details first — FK)
        String delDetail = "DELETE FROM " + x3 + ".XX10CPLANCHD WHERE XNUMPC_0 = ?";
        int delD = sqlServerJdbc.update(delDetail, tripCode);

        // Delete from XX10CPLANCHA (header)
        String delHeader = "DELETE FROM " + x3 + ".XX10CPLANCHA WHERE XNUMPC_0 = ?";
        int delH = sqlServerJdbc.update(delHeader, tripCode);

        // Update Postgres
        trip.setOptiStatus("Optimised");
        trip.setLockFlag(0);
        tripRepository.save(trip);

        log.info("Trip UNLOCKED: {} — deleted {} header, {} detail rows", tripCode, delH, delD);
    }

    // ── LOCK multiple trips ───────────────────────────────────
    @Transactional
    public void lockTrips(List<Long> tripIds, String userCode) {
        for (Long id : tripIds) {
            try {
                lockTrip(id, userCode);
            } catch (Exception e) {
                log.error("Failed to lock trip {}: {}", id, e.getMessage());
            }
        }
    }

    // ── VALIDATE multiple trips ───────────────────────────────
    @Transactional
    public void validateTrips(List<Long> tripIds, String userCode) {
        for (Long id : tripIds) {
            try {
                validateTrip(id, userCode);
            } catch (Exception e) {
                log.error("Failed to validate trip {}: {}", id, e.getMessage());
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // Private helpers
    // ═══════════════════════════════════════════════════════════

    /**
     * XX10CPLANCHA — Planning trip header (one row per trip)
     * Mirrors X3 structure for VR/trip planning header
     */
    private void writePlanningHeader(XrTrip trip, String x3, String userCode) {
        // Upsert: delete existing then insert
        sqlServerJdbc.update(
            "DELETE FROM " + x3 + ".XX10CPLANCHA WHERE XNUMPC_0 = ?",
            trip.getTripCode()
        );

        String sql = "INSERT INTO " + x3 + ".XX10CPLANCHA ("
                + "XNUMPC_0, XFCY_0, XDATE_0, XVEHCODE_0, XDRIVERID_0, "
                + "XSTARTTIME_0, XENDTIME_0, XTOTDIST_0, XTOTIME_0, "
                + "XDROPS_0, XPICKUPS_0, XSTOPS_0, XNOPACK_0, "
                + "XTOTWEIGHT_0, XTOTVOL_0, XCAPACITY_0, "
                + "XOPTISTATUS_0, XDEPSIT_0, XARRSIT_0, "
                + "XGENBY_0, XUSRCODE_0, XCREDAT_0"
                + ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        sqlServerJdbc.update(sql,
            trip.getTripCode(),
            trip.getSite(),
            trip.getDocDate(),
            trip.getVehicleCode(),
            trip.getDriverId(),
            trip.getStartTime(),
            trip.getEndTime(),
            trip.getTotalDistance(),
            trip.getTravelTime(),
            trip.getDrops(),
            trip.getPickups(),
            trip.getDrops() != null && trip.getPickups() != null
                ? trip.getDrops() + trip.getPickups() : 0,
            trip.getNoOfPackages(),
            trip.getTotalWeight(),
            trip.getTotalVolume(),
            trip.getCapacity(),
            trip.getOptiStatus(),
            trip.getDepSite(),
            trip.getArrSite(),
            trip.getGeneratedBy(),
            userCode,
            LocalDateTime.now()
        );
    }

    /**
     * XX10CPLANCHD — Planning trip detail (one row per stop)
     * Mirrors X3 structure for delivery/pickup per VR
     */
    @SuppressWarnings("unchecked")
    private void writePlanningDetails(XrTrip trip, String x3, String userCode) {
        // Delete existing details
        sqlServerJdbc.update(
            "DELETE FROM " + x3 + ".XX10CPLANCHD WHERE XNUMPC_0 = ?",
            trip.getTripCode()
        );

        // Parse stopObjects JSONB
        if (trip.getStopObjectsJson() == null || trip.getStopObjectsJson().isBlank()) {
            log.warn("No stopObjects for trip {} — skipping XX10CPLANCHD", trip.getTripCode());
            return;
        }

        List<Map<String, Object>> stops;
        try {
            stops = objectMapper.readValue(trip.getStopObjectsJson(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
        } catch (Exception e) {
            log.error("Failed to parse stopObjects for trip {}: {}", trip.getTripCode(), e.getMessage());
            return;
        }

        String sql = "INSERT INTO " + x3 + ".XX10CPLANCHD ("
                + "XNUMPC_0, SDHNUM_0, XSEQ_0, XTYPE_0, XBPCODE_0, "
                + "XADRESCODE_0, XCITY_0, XARRDATE_0, XARRTIME_0, "
                + "XDEPDATE_0, XDEPTIME_0, XSRVTIME_0, XWAITTIME_0, "
                + "XDISTFRMPREV_0, XTRVTIMEFRMPREV_0, "
                + "XNETWEIGHT_0, XVOLUME_0, XUSRCODE_0, XCREDAT_0"
                + ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        int seq = 1;
        for (Map<String, Object> stop : stops) {
            try {
                sqlServerJdbc.update(sql,
                    trip.getTripCode(),
                    getString(stop, "txn", "docNum", "id"),
                    seq++,
                    getString(stop, "type", "stopType"),
                    getString(stop, "bpcode", "bpCode"),
                    getString(stop, "addressCode", "adrescode"),
                    getString(stop, "city"),
                    getString(stop, "arrivalDate"),
                    getString(stop, "arrivalTime"),
                    getString(stop, "departureDate"),
                    getString(stop, "departureTime"),
                    getString(stop, "serviceTime"),
                    getString(stop, "waitingTime"),
                    getString(stop, "fromPrevDistance"),
                    getString(stop, "fromPrevTravelTime"),
                    getDouble(stop, "netweight", "netWeight"),
                    getDouble(stop, "vol", "volume"),
                    userCode,
                    LocalDateTime.now()
                );
            } catch (Exception e) {
                log.error("Failed to write stop {} for trip {}: {}", seq, trip.getTripCode(), e.getMessage());
            }
        }
        log.info("XX10CPLANCHD: wrote {} rows for trip {}", stops.size(), trip.getTripCode());
    }

    /**
     * XX10CLODSTOH — Load Vehicle Stock header (one row per trip)
     * Created when trip is validated — triggers LVS creation in X3
     */
    private void writeLVSHeader(XrTrip trip, String x3, String userCode) {
        // Check if already exists
        Integer existing = sqlServerJdbc.queryForObject(
            "SELECT COUNT(*) FROM " + x3 + ".XX10CLODSTOH WHERE XVRSEL_0 = ?",
            Integer.class, trip.getTripCode()
        );

        if (existing != null && existing > 0) {
            log.info("XX10CLODSTOH already exists for trip {} — skipping", trip.getTripCode());
            return;
        }

        String sql = "INSERT INTO " + x3 + ".XX10CLODSTOH ("
                + "XVRSEL_0, XFCY_0, XDATE_0, XVEHCODE_0, XDRIVERID_0, "
                + "XSTARTTIME_0, XENDTIME_0, XTOTDIST_0, "
                + "XDROPS_0, XPICKUPS_0, XNOPACK_0, "
                + "XTOTWEIGHT_0, XTOTVOL_0, "
                + "XLOADFLG_0, XDEPSIT_0, XARRSIT_0, "
                + "XUSRCODE_0, XCREDAT_0"
                + ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        sqlServerJdbc.update(sql,
            trip.getTripCode(),
            trip.getSite(),
            trip.getDocDate(),
            trip.getVehicleCode(),
            trip.getDriverId(),
            trip.getStartTime(),
            trip.getEndTime(),
            trip.getTotalDistance(),
            trip.getDrops(),
            trip.getPickups(),
            trip.getNoOfPackages(),
            trip.getTotalWeight(),
            trip.getTotalVolume(),
            1,    // XLOADFLG_0 = 1 = LVS Generated
            trip.getDepSite(),
            trip.getArrSite(),
            userCode,
            LocalDateTime.now()
        );
        log.info("XX10CLODSTOH written for trip {}", trip.getTripCode());
    }

    // ── Null-safe helpers ─────────────────────────────────────
    private String getString(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object val = map.get(key);
            if (val != null) return val.toString();
        }
        return null;
    }

    private Double getDouble(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object val = map.get(key);
            if (val instanceof Number) return ((Number) val).doubleValue();
            if (val instanceof String) {
                try { return Double.parseDouble((String) val); } catch (Exception ignored) {}
            }
        }
        return null;
    }
}
