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
    // XX10CPLANCHA — exact schema
    // ═══════════════════════════════════════════════════════════
    private void writePlanningHeader(XrTrip trip, String x3, String userCode) {
        sqlServerJdbc.update("DELETE FROM " + x3 + ".XX10CPLANCHA WHERE XNUMPC_0 = ?", trip.getTripCode());

        LocalDateTime now     = LocalDateTime.now();
        String hhmm           = now.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
        String emptyStr       = "";
        byte[] emptyUuid      = new byte[16];
        String veh            = trip.getVehicleCode() != null ? trip.getVehicleCode() : emptyStr;
        String site           = trip.getSite()        != null ? trip.getSite()        : emptyStr;
        String driverId       = trip.getDriverId()    != null ? trip.getDriverId()    : emptyStr;
        String startTime      = trip.getStartTime()   != null ? trip.getStartTime()   : emptyStr;
        String endTime        = trip.getEndTime()     != null ? trip.getEndTime()     : emptyStr;
        String arrSite        = trip.getArrSite()     != null ? trip.getArrSite()     : emptyStr;
        double totDist        = parseDoubleSafe(trip.getTotalDistance());
        double totTime        = parseDoubleSafe(trip.getTotalTime());
        java.time.LocalDate docDate = trip.getDocDate() != null ? trip.getDocDate() : java.time.LocalDate.now();
        LocalDateTime docDateDt = docDate.atStartOfDay();

        // XEQUIPID_0 .. XEQUIPID_98 = 99 empty strings
        String[] emptyEquip = new String[99];
        java.util.Arrays.fill(emptyEquip, emptyStr);

        // Build column list
        StringBuilder cols = new StringBuilder(
            "UPDTICK_0,XNUMPC_0,BPTNUM_0,CODEYVE_0,XCODEYVE_0,HEUDEP_0,"
            + "CREDAT_0,CREUSR_0,UPDUSR_0,UPDDAT_0,"
            + "OPTIMSTA_0,FCY_0,XVRY_0,JOBID_0,"
            + "TOTDISTANCE_0,TOTTIME_0,XNUMTV_0,"
            + "DATLIV_0,HEUARR_0,CREDATTIM_0,UPDDATTIM_0,AUUID_0,"
            + "DATARR_0,INSTFDR_0,INSTFCU_0,JOBSTATUS_0,"
            + "HEUEXEC_0,DATEXEC_0,DISPSTAT_0,XVALID_0,DRIVERID_0,XROUTNBR_0,"
            + "LASTUPDDAT_0,LASTUPDTIM_0,LASTUPDAUS_0,"
            + "PICKSTRT_0,CHECKIN_0,LOADINGSTR_0,LOADINGEND_0,CHECKOUT_0,RETURNED_0,"
            + "ADATLIV_0,AHEUDEP_0,ADATARR_0,AHEUARR_0,"
            + "LOADBAY_0,MASPRO_0,XFLG_0,XSTKVCR_0,XHELPER_0,XSLMAN_0,XTECHN_0,XUSER_0,"
            + "XSTATUS_0,XSMSCOUNT_0,XDIFTIME_0,XSMSSENT_0,XSEALNUMH_0,"
            + "XCIGEOX_0,XCIGEOY_0,XCOGEOX_0,XCOGEOY_0,"
            + "XUNIT_0,XUNIT1_0,XUNIT2_0,XVOLUME_0,XVOL1_0,XVOL2_0,XVOLU_0,XMASSU_0,XMASSU1_0,XVOLU1_0,"
            + "POURLOAKG_0,POURLOAM3_0,XDPRTFDR_0,XRTNFDR_0,"
            + "RHEUDEP_0,RDATLIV_0,RHEUARR_0,RDATARR_0,"
            + "TRAILER_0,TRAILER_1,"
        );
        // XEQUIPID_0..98
        for (int i = 0; i <= 98; i++) cols.append("XEQUIPID_").append(i).append("_0,");
        cols.append(
            "XOPERATION_0,XLOADBAY_0,XXSTATUS_0,XTAILGATE_0,XSOURCE_0,XLINKID_0,"
            + "XSDHPCKSTA_0,XACTDISTCKIN_0,XACTDISTCKOT_0,XOLDCODEYVE_0,"
            + "DISTANCECOST_0,ORDERCOUNT_0,OVERTIMECOST_0,REGULARTIMEC_0,"
            + "TOTALCOST_0,TOTALDISTANC_0,TOTALTIME_0,TOTALTRAVELT_0,TOTALBREAKSE_0,"
            + "RENEWALCOUNT_0,TOTALRENEWAL_0,XDESFCY_0,XTREPORT_0,XALLOCFLG_0,"
            + "XFLOCTYP_0,XTLOCTYP_0,XFLOC_0,XTLOC_0,NOTE_0"
        );

        // Build values placeholder
        int totalCols = 5+4+3+6+4+6+3+6+10+4+4+2+99+6+4+3+8+6+2+6+4;
        // easier: count manually from params list below
        String placeholders = String.join(",", java.util.Collections.nCopies(
            5+4+3+6+4+6+3+6+10+4+4+2+99+6+4+4+8+6+5+2+6+4+8+6, "?"));

        // Build params list
        java.util.List<Object> params = new java.util.ArrayList<>();
        // Core fields
        params.add(0);                    // UPDTICK_0
        params.add(trip.getTripCode());   // XNUMPC_0
        params.add(emptyStr);             // BPTNUM_0
        params.add(veh);                  // CODEYVE_0
        params.add(veh);                  // XCODEYVE_0
        params.add(startTime);            // HEUDEP_0
        // Audit
        params.add(now); params.add(userCode); params.add(userCode); params.add(now);
        // Status/IDs
        params.add(1);                    // OPTIMSTA_0 = 1
        params.add(site);                 // FCY_0
        params.add(1);                    // XVRY_0 = 1 (Scheduled)
        params.add(emptyStr);             // JOBID_0
        // Distance/time
        params.add(totDist);              // TOTDISTANCE_0
        params.add(totTime);              // TOTTIME_0
        params.add(emptyStr);             // XNUMTV_0
        // Date/time fields
        params.add(docDateDt);            // DATLIV_0
        params.add(endTime);              // HEUARR_0
        params.add(now); params.add(now); // CREDATTIM_0, UPDDATTIM_0
        params.add(emptyUuid);            // AUUID_0
        params.add(docDateDt);            // DATARR_0
        params.add(emptyStr); params.add(emptyStr); // INSTFDR_0, INSTFCU_0
        params.add(emptyStr);             // JOBSTATUS_0
        params.add(hhmm);                 // HEUEXEC_0
        params.add(now);                  // DATEXEC_0
        params.add(1);                    // DISPSTAT_0 = 1
        params.add(1);                    // XVALID_0 = 1
        params.add(driverId);             // DRIVERID_0
        params.add(0);                    // XROUTNBR_0
        // LASTUPD
        params.add(now); params.add(hhmm); params.add(userCode);
        // Status strings
        params.add(emptyStr); params.add(emptyStr); params.add(emptyStr);
        params.add(emptyStr); params.add(emptyStr); params.add(emptyStr);
        // Actual dates (same as planned initially)
        params.add(docDateDt); params.add(startTime); params.add(docDateDt); params.add(endTime);
        // Misc
        params.add(0);                    // LOADBAY_0
        params.add(0.0);                  // MASPRO_0
        params.add(0);                    // XFLG_0
        params.add(emptyStr); params.add(emptyStr); params.add(emptyStr);
        params.add(emptyStr); params.add(emptyStr); // XHELPER,XSLMAN,XTECHN,XUSER
        params.add(1);                    // XSTATUS_0 = 1
        params.add(0); params.add(0); params.add(0); // XSMSCOUNT,XDIFTIME,XSMSSENT
        params.add(emptyStr);             // XSEALNUMH_0
        // GEO
        params.add(emptyStr); params.add(emptyStr); params.add(emptyStr); params.add(emptyStr);
        // Units
        params.add(emptyStr); params.add(emptyStr); params.add(emptyStr);
        params.add(emptyStr); params.add(emptyStr); params.add(emptyStr);
        params.add(emptyStr); params.add(emptyStr); params.add(emptyStr); params.add(emptyStr);
        // Load pct
        params.add(0.0); params.add(0.0);
        params.add(0); params.add(0);    // XDPRTFDR, XRTNFDR
        // Return times
        params.add(emptyStr); params.add(docDateDt); params.add(emptyStr); params.add(docDateDt);
        // Trailers
        params.add(emptyStr); params.add(emptyStr);
        // XEQUIPID_0..98
        for (int i = 0; i <= 98; i++) params.add(emptyStr);
        // Tail fields
        params.add(0); params.add(0);    // XOPERATION_0, XLOADBAY_0
        params.add(emptyStr);            // XXSTATUS_0
        params.add(0); params.add(0);   // XTAILGATE_0, XSOURCE_0
        params.add(emptyStr);            // XLINKID_0
        params.add(0);                   // XSDHPCKSTA_0
        params.add(0.0); params.add(0.0); // XACTDISTCKIN_0, XACTDISTCKOT_0
        params.add(emptyStr);            // XOLDCODEYVE_0
        // Cost fields
        params.add(emptyStr);            // DISTANCECOST_0
        params.add(emptyStr);            // ORDERCOUNT_0
        params.add(emptyStr);            // OVERTIMECOST_0
        params.add(emptyStr);            // REGULARTIMEC_0
        params.add(trip.getTotalCost()     != null ? trip.getTotalCost()     : emptyStr); // TOTALCOST_0
        params.add(trip.getTotalDistance() != null ? trip.getTotalDistance() : emptyStr); // TOTALDISTANC_0
        params.add(trip.getTotalTime()     != null ? trip.getTotalTime()     : emptyStr); // TOTALTIME_0
        params.add(trip.getTravelTime()    != null ? trip.getTravelTime()    : emptyStr); // TOTALTRAVELT_0
        params.add(emptyStr);            // TOTALBREAKSE_0
        params.add(emptyStr); params.add(emptyStr); // RENEWALCOUNT_0, TOTALRENEWAL_0
        params.add(arrSite);             // XDESFCY_0
        params.add(0); params.add(0);   // XTREPORT_0, XALLOCFLG_0
        params.add(emptyStr); params.add(emptyStr); // XFLOCTYP_0, XTLOCTYP_0
        params.add(emptyStr); params.add(emptyStr); // XFLOC_0, XTLOC_0
        params.add(emptyStr);            // NOTE_0

        String sql = "INSERT INTO " + x3 + ".XX10CPLANCHA (" + cols + ") VALUES ("
            + String.join(",", java.util.Collections.nCopies(params.size(), "?")) + ")";

        sqlServerJdbc.update(sql, params.toArray());
        log.info("XX10CPLANCHA written for {}", trip.getTripCode());
    }

    private double parseDoubleSafe(String val) {
        try { return val != null && !val.isBlank() ? Double.parseDouble(val) : 0.0; }
        catch (Exception e) { return 0.0; }
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
    // XX10CLODSTOH — exact schema
    // ═══════════════════════════════════════════════════════════
    private void writeLVSHeader(XrTrip trip, String x3, String userCode) {
        Integer cnt = sqlServerJdbc.queryForObject(
            "SELECT COUNT(*) FROM " + x3 + ".XX10CLODSTOH WHERE XVRSEL_0 = ?",
            Integer.class, trip.getTripCode());
        if (cnt != null && cnt > 0) { log.info("LVS already exists for {}", trip.getTripCode()); return; }

        LocalDateTime now    = LocalDateTime.now();
        String e             = "";                     // empty string
        byte[] emptyUuid     = new byte[16];
        String veh           = trip.getVehicleCode() != null ? trip.getVehicleCode() : e;
        String site          = trip.getSite()        != null ? trip.getSite()        : e;
        String driverId      = trip.getDriverId()    != null ? trip.getDriverId()    : e;
        String startTime     = trip.getStartTime()   != null ? trip.getStartTime()   : e;
        String endTime       = trip.getEndTime()     != null ? trip.getEndTime()     : e;
        String arrSite       = trip.getArrSite()     != null ? trip.getArrSite()     : e;
        double capWeight     = parseDoubleSafe(trip.getTotalWeight());
        java.time.LocalDate  doc   = trip.getDocDate() != null ? trip.getDocDate() : java.time.LocalDate.now();
        LocalDateTime docDt  = doc.atStartOfDay();

        // Generate LVS number: LVS-{tripCode}
        String lvsNum = "LVS-" + trip.getTripCode();

        java.util.List<Object> p = new java.util.ArrayList<>();
        p.add(0);           // UPDTICK_0
        p.add(lvsNum);      // VCRNUM_0  — generated LVS number
        p.add(0);           // BETFCYCOD_0 tinyint
        p.add(site);        // STOFCY_0
        p.add(e);           // FCYDES_0
        p.add(site);        // PURFCY_0
        p.add(site);        // SALFCY_0
        p.add(e);           // FCYADD_0
        p.add(e);           // BPSNUM_0
        p.add(e);           // BPSADD_0
        p.add(e);           // SCOLOC_0
        p.add(e);           // PJT_0
        p.add(e);           // BPCNUM_0
        p.add(e);           // CUR_0
        p.add(0);           // BETCPY_0
        p.add(0);           // INVSGH_0
        p.add(0);           // INVFLG_0
        p.add(e);           // SIHNUM_0
        p.add(docDt);       // IPTDAT_0 — trip date
        p.add(e);           // VCRDES_0
        p.add(e);           // TRSFAM_0
        p.add(0);           // TRSTYP_0
        p.add(e);           // TRSCOD_0
        p.add(e);           // ENTCOD_0
        // DIE_0..19
        for (int i=0;i<=19;i++) p.add(e);
        // CCE_0..19
        for (int i=0;i<=19;i++) p.add(e);
        p.add(e);           // WRHE_0
        p.add(0);           // EXPNUM_0
        p.add(0);           // IMPNUMLIG_0
        p.add(now);         // CREDAT_0
        p.add(userCode);    // CREUSR_0
        p.add(now);         // UPDDAT_0
        p.add(userCode);    // UPDUSR_0
        p.add(now);         // CREDATTIM_0
        p.add(now);         // UPDDATTIM_0
        p.add(emptyUuid);   // AUUID_0
        p.add(0);           // CFMFLG_0
        p.add(e);           // SGHTYP_0
        p.add(e);           // TMPSGHNUM_0
        p.add(e);           // MANDOC_0
        p.add(e);           // ATDTCOD_0
        p.add(docDt);       // DPEDAT_0 — departure date
        p.add(startTime);   // ETD_0 — start time
        p.add(docDt);       // ARVDAT_0 — arrival date
        p.add(endTime);     // ETA_0 — end time
        p.add(veh);         // LICPLATE_0 — vehicle code
        p.add(veh);         // TRLLICPLATE_0 — vehicle code
        p.add(driverId);    // DRIVERID_0
        p.add(e);           // XSALEMEN_0
        p.add(e);           // XOPERATOR_0
        p.add(e);           // XTECHN_0
        p.add(driverId);    // XAPPUSR_0 — driver id
        p.add(lvsNum);      // XVCRNUM_0 — LVS number
        p.add(0);           // XRETURNFLG_0
        p.add(0);           // XACTFLG_0
        p.add(0);           // XBUSTYP1_0
        p.add(0);           // XBUSTYP2_0
        p.add(trip.getTripCode()); // XVRSEL_0 — tripCode
        p.add(e);           // XLOADREF_0
        p.add(veh);         // CODEYVE_0 — vehicle code
        p.add(1);           // XVALFLG_0
        p.add(e);           // LOCSEL_0
        p.add(0);           // XROUTNBR_0 — trip seq
        p.add(e);           // XTEXTNUM_0
        p.add(0.0);         // XTOTNONSTK_0
        p.add(veh);         // XCODEYVE_0 — vehicle code
        p.add(1);           // XLOADFLG_0 = 1 Generated
        p.add(0);           // X10CHKIN_0
        p.add(docDt);       // XXIPTDAT_0 — trip date
        p.add(0);           // XSCHREALC_0
        p.add(capWeight);   // XCAPACITIES_0 — trip weight
        p.add(0.0);         // XVEHVOL_0
        p.add(0.0);         // XTOTSHESTK_0
        p.add(e);           // XSEALNUMH_0
        p.add(0);           // XUNLOADFLG_0
        p.add(0);           // XSTARTODMTR_0
        p.add(0);           // XENDODMTR_0
        p.add(now);         // XCHKINDAT_0
        p.add(e);           // XCHKINTIM_0
        p.add(now);         // XCHKOUDAT_0
        p.add(e);           // XCHKOUTIM_0
        p.add(e); p.add(e); p.add(e); p.add(e); p.add(e); // XPMASS,XPVOL,XMASS,XVMASS,XLMASS
        p.add(e); p.add(e); p.add(e);  // XVOLCAM,XVEHV,XMPVOL
        p.add(now);         // XUNLOADDATE_0
        p.add(e);           // XUNLOADEDBY_0
        p.add(e);           // XUNLOADTIME_0
        p.add(e); p.add(e); p.add(e); // TRAILER_0, TRAILER_1, TRAILER2_0
        p.add(docDt);       // XVRDATE_0 — trip date
        p.add(e);           // XSOURCELOC_0
        p.add(0); p.add(0); // XLOADBAYD_0, XLOADBAYR_0
        p.add(e);           // XDEVICEID_0
        p.add(e);           // XOLDCODEYVE_0
        p.add(arrSite);     // XDESFCY_0
        p.add(0);           // XBUSTYP3_0
        p.add(e);           // XBPTNUM_0 — carrier
        p.add(veh);         // XECODEYVE_0 — vehicle code
        p.add(driverId);    // XEDRIVERID_0 — driver code
        p.add(e);           // XEREGSTR_0
        p.add(e);           // XETRAILER_0
        p.add(e);           // XETREGSTR_0
        p.add(e); p.add(e); // XLOG_0, XPDLOG_0
        p.add(0);           // XSTOVAL_0
        p.add(e);           // XMOB_0
        p.add(e);           // XWEB_0
        p.add(0);           // XFORSEQ_0
        p.add(e); p.add(e); p.add(e); // XLOADUSR_0, XLOADNAM_0, XLOADEML_0
        p.add(e);           // XDRN_0
        p.add(0); p.add(0); // XPODSUB_0, XPODSTATUS_0
        p.add(0); p.add(0); // XLODAPPSTA_0, XROUTSTAT_0
        p.add(0);           // XTRIP_0 — trip seq
        p.add(e);           // MDL_0
        p.add(0);           // XMLDUSER_0
        p.add(e);           // XPOHNUM_0
        p.add(0);           // XSTATUS_0
        p.add(0);           // XALLFLG_0
        p.add(e);           // XROUTERSA_0
        p.add(e);           // XNOTE1_0

        String cols =
            "UPDTICK_0,VCRNUM_0,BETFCYCOD_0,STOFCY_0,FCYDES_0,PURFCY_0,SALFCY_0,FCYADD_0,"
            + "BPSNUM_0,BPSADD_0,SCOLOC_0,PJT_0,BPCNUM_0,CUR_0,BETCPY_0,INVSGH_0,INVFLG_0,"
            + "SIHNUM_0,IPTDAT_0,VCRDES_0,TRSFAM_0,TRSTYP_0,TRSCOD_0,ENTCOD_0,"
            + "DIE_0,DIE_1,DIE_2,DIE_3,DIE_4,DIE_5,DIE_6,DIE_7,DIE_8,DIE_9,"
            + "DIE_10,DIE_11,DIE_12,DIE_13,DIE_14,DIE_15,DIE_16,DIE_17,DIE_18,DIE_19,"
            + "CCE_0,CCE_1,CCE_2,CCE_3,CCE_4,CCE_5,CCE_6,CCE_7,CCE_8,CCE_9,"
            + "CCE_10,CCE_11,CCE_12,CCE_13,CCE_14,CCE_15,CCE_16,CCE_17,CCE_18,CCE_19,"
            + "WRHE_0,EXPNUM_0,IMPNUMLIG_0,"
            + "CREDAT_0,CREUSR_0,UPDDAT_0,UPDUSR_0,CREDATTIM_0,UPDDATTIM_0,AUUID_0,"
            + "CFMFLG_0,SGHTYP_0,TMPSGHNUM_0,MANDOC_0,ATDTCOD_0,"
            + "DPEDAT_0,ETD_0,ARVDAT_0,ETA_0,LICPLATE_0,TRLLICPLATE_0,"
            + "DRIVERID_0,XSALEMEN_0,XOPERATOR_0,XTECHN_0,XAPPUSR_0,"
            + "XVCRNUM_0,XRETURNFLG_0,XACTFLG_0,XBUSTYP1_0,XBUSTYP2_0,"
            + "XVRSEL_0,XLOADREF_0,CODEYVE_0,XVALFLG_0,LOCSEL_0,"
            + "XROUTNBR_0,XTEXTNUM_0,XTOTNONSTK_0,XCODEYVE_0,XLOADFLG_0,"
            + "X10CHKIN_0,XXIPTDAT_0,XSCHREALC_0,XCAPACITIES_0,XVEHVOL_0,"
            + "XTOTSHESTK_0,XSEALNUMH_0,XUNLOADFLG_0,XSTARTODMTR_0,XENDODMTR_0,"
            + "XCHKINDAT_0,XCHKINTIM_0,XCHKOUDAT_0,XCHKOUTIM_0,"
            + "XPMASS_0,XPVOL_0,XMASS_0,XVMASS_0,XLMASS_0,XVOLCAM_0,XVEHV_0,XMPVOL_0,"
            + "XUNLOADDATE_0,XUNLOADEDBY_0,XUNLOADTIME_0,"
            + "TRAILER_0,TRAILER_1,TRAILER2_0,XVRDATE_0,XSOURCELOC_0,"
            + "XLOADBAYD_0,XLOADBAYR_0,XDEVICEID_0,XOLDCODEYVE_0,XDESFCY_0,"
            + "XBUSTYP3_0,XBPTNUM_0,XECODEYVE_0,XEDRIVERID_0,XEREGSTR_0,"
            + "XETRAILER_0,XETREGSTR_0,XLOG_0,XPDLOG_0,XSTOVAL_0,"
            + "XMOB_0,XWEB_0,XFORSEQ_0,XLOADUSR_0,XLOADNAM_0,XLOADEML_0,"
            + "XDRN_0,XPODSUB_0,XPODSTATUS_0,XLODAPPSTA_0,XROUTSTAT_0,"
            + "XTRIP_0,MDL_0,XMLDUSER_0,XPOHNUM_0,XSTATUS_0,XALLFLG_0,XROUTERSA_0,XNOTE1_0";

        String sql = "INSERT INTO " + x3 + ".XX10CLODSTOH (" + cols + ") VALUES ("
            + String.join(",", java.util.Collections.nCopies(p.size(), "?")) + ")";

        sqlServerJdbc.update(sql, p.toArray());
        log.info("XX10CLODSTOH ({} fields) written for trip {}", p.size(), trip.getTripCode());
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
