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
public class TripLockService {

    private final TripRepository tripRepository;
    private final SchemaConfig   schemas;
    private final ObjectMapper   objectMapper;
    private final JdbcTemplate   sqlServerJdbc;

    public TripLockService(
            TripRepository tripRepository,
            SchemaConfig schemas,
            ObjectMapper objectMapper,
            @Qualifier("sqlServerJdbcTemplate") JdbcTemplate sqlServerJdbc) {
        this.tripRepository = tripRepository;
        this.schemas        = schemas;
        this.objectMapper   = objectMapper;
        this.sqlServerJdbc  = sqlServerJdbc;
    }

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
    // XX10CPLANCHD — exact schema with correct field names
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

        LocalDateTime now = LocalDateTime.now();
        String emptyStr   = "";
        byte[] emptyUuid  = new byte[16];

        String sql = "INSERT INTO " + x3 + ".XX10CPLANCHD ("
            // Identity / keys
            + "UPDTICK_0, XNUMPC_0, XLINPC_0, SDHNUM_0, XPICK_SDH_0, XDTYPE_0,"
            // Audit
            + "CREDAT_0, CREUSR_0, UPDUSR_0, UPDDAT_0, CREDATTIM_0, UPDDATTIM_0,"
            // Sequence + distances
            + "SEQUENCE_0, FROMPREVDIST_0, FROMPREVTRA_0,"
            // Planned arrival
            + "ARRIVEDATE_0, AARRIVEDATE_0, ARRIVETIME_0, AARRIVETIME_0,"
            // Planned departure
            + "DEPARTDATE_0, ADEPARTDATE_0, DEPARTTIME_0, ADEPARTTIME_0,"
            // UTC times (same as planned)
            + "ARRIVEDATEUT_0, ARRIVETIMEUT_0, DEPARTDATEU_0, DEPARTTIMEU_0,"
            // RAINONAFF (5 slots)
            + "RAINONAFF_0, RAINONAFF_1, RAINONAFF_2, RAINONAFF_3, RAINONAFF_4,"
            // Flags
            + "OPTISTA_0, XLOADED_0, XACTETA_0, XACTETD_0,"
            + "XSMSFLG_0, XSDHSKIP_0, XDEPFLG_0, XDLV_STATUS_0,"
            // Measurements
            + "XMS_0, XVOL_0, SERVICETIME_0, XCALCDIS_0, XWAITTIME_0, XMAXSTAHT_0,"
            + "SWAITTIME_0, SERVICETIM_0,"
            // Doc info
            + "XDOCTYP_0, XPICKUP_DROP_0, XSEALNUM_0, XSKIPRES_0, XACTSEQ_0,"
            // Return times (empty)
            + "RDEPARTDATE_0, RDEPARTTIME_0, RARRIVEDATE_0, RARRIVETIME_0,"
            // Confirm times (empty)
            + "XCNFARRDATE_0, XCNFARRTIME_0, XCNFDEPDATE_0, XCNFDEPTIME_0,"
            // Misc
            + "XDOCSTA_0, XACTDISTMTS_0, XDOCSITE_0, XBREAKTYP_0, XLOADBAY_0,"
            + "XSPECIFICRES_0, AUUID_0"
            + ") VALUES ("
            + "0,?,?,?,?,1,"           // UPDTICK_0=0, XNUMPC_0, XLINPC_0, SDHNUM_0, XPICK_SDH_0=empty, XDTYPE_0=1
            + "?,?,?,?,?,?,"           // CREDAT_0, CREUSR_0, UPDUSR_0, UPDDAT_0, CREDATTIM_0, UPDDATTIM_0
            + "?,?,?,"                 // SEQUENCE_0, FROMPREVDIST_0, FROMPREVTRA_0
            + "?,?,?,?,"               // ARRIVEDATE_0, AARRIVEDATE_0, ARRIVETIME_0, AARRIVETIME_0
            + "?,?,?,?,"               // DEPARTDATE_0, ADEPARTDATE_0, DEPARTTIME_0, ADEPARTTIME_0
            + "?,?,?,?,"               // ARRIVEDATEUT_0, ARRIVETIMEUT_0, DEPARTDATEU_0, DEPARTTIMEU_0
            + "?,?,?,?,?,"             // RAINONAFF 0-4
            + "1,0,?,?,0,0,0,1,"       // OPTISTA_0=1, XLOADED_0=0, XACTETA_0, XACTETD_0, flags, XDLV_STATUS_0=1
            + "?,?,?,0,?,0,0,?,"       // XMS_0, XVOL_0, SERVICETIME_0, XCALCDIS_0=0, XWAITTIME_0, SWAITTIME_0=0, SERVICETIM_0
            + "1,?,?,?,0,"             // XDOCTYP_0=1, XPICKUP_DROP_0, XSEALNUM_0, XSKIPRES_0, XACTSEQ_0=0
            + "?,?,?,?,"               // RDEPARTDATE_0, RDEPARTTIME_0, RARRIVEDATE_0, RARRIVETIME_0
            + "?,?,?,?,"               // XCNFARRDATE_0, XCNFARRTIME_0, XCNFDEPDATE_0, XCNFDEPTIME_0
            + "0,0,?,0,0,?,?"          // XDOCSTA_0=0, XACTDISTMTS_0=0, XDOCSITE_0, XBREAKTYP_0=0, XLOADBAY_0=0, XSPECIFICRES_0, AUUID_0
            + ")";

        int seq = 1;
        for (Map<String, Object> s : stops) {
            try {
                String docNum    = getString(s, "txn", "docNum", "id");
                String arrDate   = getString(s, "arrivalDate");
                String arrTime   = getString(s, "arrivalTime");
                String depDate   = getString(s, "departureDate");
                String depTime   = getString(s, "departureTime");
                String srvTime   = getString(s, "serviceTime");
                String waitTime  = getString(s, "waitingTime");
                String prevDist  = getString(s, "fromPrevDistance");
                String prevTravel= getString(s, "fromPrevTravelTime");
                String stopType  = getString(s, "type", "stopType");
                String site      = trip.getSite();

                // Parse dates
                LocalDateTime arrDT  = parseDateTime(arrDate, arrTime);
                LocalDateTime depDT  = parseDateTime(depDate, depTime);

                // numeric values
                double prevDistNum   = parseDouble(prevDist);
                double prevTravelNum = parseDouble(prevTravel);
                double waitNum       = parseDouble(waitTime);

                // DROP=1, PICKUP=2
                int pickupDrop = "PICKUP".equals(stopType) ? 2 : 1;

                sqlServerJdbc.update(sql,
                    // Keys
                    trip.getTripCode(),          // XNUMPC_0
                    seq * 1000,                  // XLINPC_0 = line * 1000
                    docNum != null ? docNum : emptyStr, // SDHNUM_0
                    emptyStr,                    // XPICK_SDH_0
                    // Audit
                    now, userCode, userCode, now, now, now,
                    // Sequence + distances
                    seq,                         // SEQUENCE_0
                    prevDistNum,                 // FROMPREVDIST_0
                    prevTravelNum,               // FROMPREVTRA_0
                    // Planned arrival
                    arrDT, arrDT,                // ARRIVEDATE_0, AARRIVEDATE_0
                    arrTime != null ? arrTime : emptyStr,  // ARRIVETIME_0
                    arrTime != null ? arrTime : emptyStr,  // AARRIVETIME_0
                    // Planned departure
                    depDT, depDT,                // DEPARTDATE_0, ADEPARTDATE_0
                    depTime != null ? depTime : emptyStr,  // DEPARTTIME_0
                    depTime != null ? depTime : emptyStr,  // ADEPARTTIME_0
                    // UTC (same as planned)
                    arrDT, arrTime != null ? arrTime : emptyStr,
                    depDT, depTime != null ? depTime : emptyStr,
                    // RAINONAFF 0-4
                    emptyStr, emptyStr, emptyStr, emptyStr, emptyStr,
                    // XACTETA_0, XACTETD_0
                    arrTime != null ? arrTime : emptyStr,
                    depTime != null ? depTime : emptyStr,
                    // XMS_0, XVOL_0, SERVICETIME_0, XWAITTIME_0, SERVICETIM_0
                    emptyStr, emptyStr,
                    srvTime != null ? srvTime : emptyStr,
                    waitNum,
                    srvTime != null ? srvTime : emptyStr,
                    // XPICKUP_DROP_0, XSEALNUM_0, XSKIPRES_0
                    pickupDrop, emptyStr, emptyStr,
                    // RDEPARTDATE_0, RDEPARTTIME_0, RARRIVEDATE_0, RARRIVETIME_0
                    now, emptyStr, now, emptyStr,
                    // XCNFARRDATE_0, XCNFARRTIME_0, XCNFDEPDATE_0, XCNFDEPTIME_0
                    now, emptyStr, now, emptyStr,
                    // XDOCSITE_0, XSPECIFICRES_0, AUUID_0
                    site != null ? site : emptyStr,
                    emptyStr,
                    emptyUuid
                );
                seq++;
            } catch (Exception e) {
                log.error("XX10CPLANCHD stop {} for {}: {}", seq, trip.getTripCode(), e.getMessage());
            }
        }
        log.info("XX10CPLANCHD: {} rows written for {}", stops.size(), trip.getTripCode());
    }

    private LocalDateTime parseDateTime(String date, String time) {
        try {
            if (date == null || date.isBlank()) return LocalDateTime.now();
            String d = date.trim();
            String t = (time != null && !time.isBlank()) ? time.trim().substring(0, 5) : "00:00";
            return java.time.LocalDateTime.parse(d + "T" + t + ":00");
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    private double parseDouble(String val) {
        try { return val != null ? Double.parseDouble(val) : 0.0; }
        catch (Exception e) { return 0.0; }
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
