package com.transport.tms.Trip.Lock;

import com.transport.tms.Config.SchemaConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * VR / VRD / LVS read endpoints — mirrors old CBTTL system
 *
 * GET /api/v1/transport/vr?vrcode=VR-KCC01-20260624-001
 *     → XX10CPLANCHA header row
 *
 * GET /api/v1/transport/vrdetails?vrcode=VR-KCC01-20260624-001
 *     → XX10CPLANCHD stop rows
 *
 * GET /api/v1/transport/loadvehstk?vrcode=VR-KCC01-20260624-001
 *     → XX10CLODSTOH LVS row
 */
@RestController
@RequestMapping("/api/v1/transport")
public class VrController {

    private final SchemaConfig schemas;
    private final JdbcTemplate sqlServerJdbc;

    public VrController(
            SchemaConfig schemas,
            @Qualifier("sqlServerJdbcTemplate") JdbcTemplate sqlServerJdbc) {
        this.schemas       = schemas;
        this.sqlServerJdbc = sqlServerJdbc;
    }

    // ── VR Header — XX10CPLANCHA ──────────────────────────────
    @GetMapping("/vr")
    public Map<String, Object> getVr(@RequestParam String vrcode) {
        String x3  = schemas.getX3Schema();
        String sql = "SELECT XNUMPC_0 AS xnumpc, CODEYVE_0 AS codeyve,"
                   + " XCODEYVE_0, FCY_0 AS fcy, DRIVERID_0 AS driverid,"
                   + " HEUDEP_0 AS heudep, HEUARR_0 AS heuarr,"
                   + " HEUEXEC_0 AS heuexec, DATEXEC_0 AS datexec,"
                   + " DATLIV_0 AS datliv, DATARR_0 AS datarr,"
                   + " ADATLIV_0 AS adatliv, ADATARR_0 AS adatarr,"
                   + " AHEUDEP_0 AS aheudep, AHEUARR_0 AS aheuarr,"
                   + " OPTIMSTA_0 AS optimsta, DISPSTAT_0 AS dispstat,"
                   + " XVRY_0 AS xvry, XVALID_0 AS xvalid,"
                   + " XSTATUS_0 AS xstatus, XROUTNBR_0 AS xroutnbr,"
                   + " TOTDISTANCE_0 AS totdistance, TOTTIME_0 AS tottime,"
                   + " BPTNUM_0 AS bptnum, XDESFCY_0 AS xdesfcy,"
                   + " TOTALCOST_0 AS totalcost, TOTALDISTANC_0 AS totaldistanc,"
                   + " TOTALTIME_0 AS totaltime, TOTALTRAVELT_0 AS totaltravelt,"
                   + " TRAILER_0 AS trailer, XLOADBAY_0 AS loadbay,"
                   + " JOBID_0 AS jobid, JOBSTATUS_0 AS jobstatus"
                   + " FROM " + x3 + ".XX10CPLANCHA"
                   + " WHERE XNUMPC_0 = ?";

        List<Map<String, Object>> rows = sqlServerJdbc.queryForList(sql, vrcode);
        return rows.isEmpty() ? Collections.emptyMap() : rows.get(0);
    }

    // ── VR Details — XX10CPLANCHD ─────────────────────────────
    @GetMapping("/vrdetails")
    public List<Map<String, Object>> getVrDetails(@RequestParam String vrcode) {
        String x3  = schemas.getX3Schema();
        String sql = "SELECT XNUMPC_0 AS xnumpc, SDHNUM_0 AS sdhnum,"
                   + " XLINPC_0 AS xlinpc, SEQUENCE_0 AS sequence,"
                   + " XDTYPE_0 AS xdtype, XPICKUP_DROP_0 AS pickupdrop,"
                   + " ARRIVEDATE_0 AS arrivedate, ARRIVETIME_0 AS arrivetime,"
                   + " DEPARTDATE_0 AS departdate, DEPARTTIME_0 AS departtime,"
                   + " AARRIVEDATE_0 AS aarrivedate, AARRIVETIME_0 AS aarrivetime,"
                   + " ADEPARTDATE_0 AS adepartdate, ADEPARTTIME_0 AS adeparttime,"
                   + " FROMPREVDIST_0 AS fromprevdist, FROMPREVTRA_0 AS fromprevtra,"
                   + " SERVICETIME_0 AS servicetime, XWAITTIME_0 AS waittime,"
                   + " OPTISTA_0 AS optista, XLOADED_0 AS xloaded,"
                   + " XDLV_STATUS_0 AS xdlvstatus, XDOCSITE_0 AS xdocsite,"
                   + " XACTETA_0 AS xacteta, XACTETD_0 AS xactetd,"
                   + " XCALCDIS_0 AS xcalcdis, XACTSEQ_0 AS xactseq"
                   + " FROM " + x3 + ".XX10CPLANCHD"
                   + " WHERE XNUMPC_0 = ?"
                   + " ORDER BY SEQUENCE_0";

        return sqlServerJdbc.queryForList(sql, vrcode);
    }

    // ── LVS Header — XX10CLODSTOH ─────────────────────────────
    @GetMapping("/loadvehstk")
    public Map<String, Object> getLvs(@RequestParam String vrcode) {
        String x3  = schemas.getX3Schema();
        String sql = "SELECT VCRNUM_0 AS vcrnum, XVRSEL_0 AS xvrsel,"
                   + " STOFCY_0 AS stofcy, SALFCY_0 AS salfcy,"
                   + " DRIVERID_0 AS driverid, CODEYVE_0 AS codeyve,"
                   + " XCODEYVE_0, LICPLATE_0 AS licplate,"
                   + " DPEDAT_0 AS dpedat, ETD_0 AS etd,"
                   + " ARVDAT_0 AS arvdat, ETA_0 AS eta,"
                   + " IPTDAT_0 AS iptdat, XVRDATE_0 AS xvrdate,"
                   + " XLOADFLG_0 AS xloadflg, XVALFLG_0 AS xvalflg,"
                   + " XCAPACITIES_0 AS xcapacities, XVEHVOL_0 AS xvehvol,"
                   + " XROUTNBR_0 AS xroutnbr, XTRIP_0 AS xtrip,"
                   + " XDESFCY_0 AS xdesfcy, XBPTNUM_0 AS xbptnum,"
                   + " CREDAT_0 AS credat, CREUSR_0 AS creusr,"
                   + " XAPPUSR_0 AS xappusr, XSTATUS_0 AS xstatus"
                   + " FROM " + x3 + ".XX10CLODSTOH"
                   + " WHERE XVRSEL_0 = ?";

        List<Map<String, Object>> rows = sqlServerJdbc.queryForList(sql, vrcode);
        return rows.isEmpty() ? Collections.emptyMap() : rows.get(0);
    }
}
