package com.transport.tms.Trip.Lock;

import com.transport.tms.Fleet.Entity.Driver;
import com.transport.tms.Fleet.Entity.Vehicle;
import com.transport.tms.Fleet.Repository.DriverRepository;
import com.transport.tms.Fleet.Repository.VehicleRepository;
import com.transport.tms.Trip.Lock.Entity.LvsHeader;
import com.transport.tms.Trip.Lock.Entity.VrDetail;
import com.transport.tms.Trip.Lock.Entity.VrHeader;
import com.transport.tms.Trip.Lock.Repository.LvsHeaderRepository;
import com.transport.tms.Trip.Lock.Repository.VrDetailRepository;
import com.transport.tms.Trip.Lock.Repository.VrHeaderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * VR / VRD / LVS read endpoints — now reads from xr_vrheader/xr_vrdetails/
 * xr_lvsheader (Postgres) instead of XX10CPLANCHA/XX10CPLANCHD/XX10CLODSTOH
 * (X3 SQL Server). Response field names/shape are kept identical to what
 * the frontend already consumes — only the data source changed.
 *
 * GET /api/v1/transport/vr?vrcode=VR-KCC01-20260624-001
 * GET /api/v1/transport/vrdetails?vrcode=VR-KCC01-20260624-001
 * GET /api/v1/transport/loadvehstk?vrcode=VR-KCC01-20260624-001
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/transport")
@RequiredArgsConstructor
public class VrController {

    private final VrHeaderRepository  vrHeaderRepo;
    private final VrDetailRepository  vrDetailRepo;
    private final LvsHeaderRepository lvsHeaderRepo;
    private final VehicleRepository   vehicleRepo;
    private final DriverRepository    driverRepo;

    // ── VR Header — xr_vrheader + enrichment ──────────────────
    @GetMapping("/vr")
    public Map<String, Object> getVr(@RequestParam String vrcode) {
        Optional<VrHeader> found = vrHeaderRepo.findById(vrcode);
        if (found.isEmpty()) return Collections.emptyMap();
        VrHeader h = found.get();

        Map<String, Object> vr = new LinkedHashMap<>();
        vr.put("xnumpc", h.getTripCode());
        vr.put("codeyve", h.getVehicleCode());
        vr.put("fcy", h.getSite());
        vr.put("driverid", h.getDriverId());
        vr.put("heudep", h.getStartTime());
        vr.put("heuarr", h.getEndTime());
        vr.put("datliv", h.getDocDate());
        vr.put("datarr", h.getDocDate());
        vr.put("optimsta", h.getStatus());
        vr.put("dispstat", h.getStatus());
        vr.put("xvalid", h.getStatus());
        vr.put("xstatus", h.getStatus());
        vr.put("totdistance", h.getTotalDistance());
        vr.put("tottime", h.getTotalTime());
        vr.put("xdesfcy", h.getArrSite());
        vr.put("totalcost", h.getTotalCost());
        vr.put("totaldistanc", h.getTotalDistance());
        vr.put("totaltime", h.getTotalTime());
        vr.put("totaltravelt", h.getTravelTime());

        // ── Enrich: Vehicle class/category + image ────────────
        String vehicleCode = h.getVehicleCode();
        if (vehicleCode != null) {
            vehicleRepo.findById(vehicleCode).ifPresent(v -> {
                if (v.getCategory() != null) {
                    vr.put("vehicleClass",     v.getCategory().getCategoryCode());
                    vr.put("vehicleClassDesc", v.getCategory().getDescription());
                    vr.put("vehclass",         v.getCategory().getCategoryCode()); // CBTTL compat
                }
                if (v.getVehicleImage() != null && v.getVehicleImage().length > 0) {
                    vr.put("vehicleImage", "data:image/jpeg;base64,"
                        + Base64.getEncoder().encodeToString(v.getVehicleImage()));
                }
                vr.put("vehicleName",   v.getVehicleName());
                vr.put("vehicleNumber", v.getVehicleNumber());
                vr.put("site",          v.getSite());
            });
        }

        // ── Enrich: Driver name + image ───────────────────────
        String driverId = h.getDriverId();
        if (driverId != null) {
            driverRepo.findById(driverId).ifPresent(d -> {
                vr.put("driverName",   d.getDriverName());
                vr.put("driverMobile", d.getMobileNo());
                if (d.getDriverImage() != null && d.getDriverImage().length > 0) {
                    vr.put("driverImage", "data:image/jpeg;base64,"
                        + Base64.getEncoder().encodeToString(d.getDriverImage()));
                }
            });
        }

        // ── LVS status (if validated) ─────────────────────────
        Optional<LvsHeader> lvs = lvsHeaderRepo.findByTripCode(vrcode);
        if (lvs.isPresent()) {
            vr.put("lvsNumber",  lvs.get().getLvsNumber());
            vr.put("loadStatus", lvs.get().getLoadFlag());
            vr.put("validated",  true);
        } else {
            vr.put("validated", false);
        }

        // ── Creation time format MM-DD-YYYY HH:mm ─────────────
        if (h.getCreatedAt() != null) {
            vr.put("creationTime", h.getCreatedAt().format(DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm")));
        }

        return vr;
    }

    // ── VR Details — xr_vrdetails ─────────────────────────────
    @GetMapping("/vrdetails")
    public List<Map<String, Object>> getVrDetails(@RequestParam String vrcode) {
        List<VrDetail> details = vrDetailRepo.findByTripCodeOrderBySeqAsc(vrcode);
        List<Map<String, Object>> out = new ArrayList<>();
        for (VrDetail d : details) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("xnumpc", d.getTripCode());
            row.put("sdhnum", d.getDocNum());
            row.put("xlinpc", d.getLineNum());
            row.put("sequence", d.getSeq());
            row.put("xdtype", 1);
            row.put("pickupdrop", d.getPickupDrop());
            row.put("arrivedate", d.getArrivalDate());
            row.put("arrivetime", d.getArrivalTime());
            row.put("departdate", d.getDepartureDate());
            row.put("departtime", d.getDepartureTime());
            row.put("aarrivedate", d.getArrivalDate());
            row.put("aarrivetime", d.getArrivalTime());
            row.put("adepartdate", d.getDepartureDate());
            row.put("adeparttime", d.getDepartureTime());
            row.put("fromprevdist", d.getFromPrevDistance());
            row.put("fromprevtra", d.getFromPrevTravelTime());
            row.put("servicetime", d.getServiceTime());
            row.put("waittime", d.getWaitingTime());
            row.put("optista", 1);
            row.put("xloaded", 0);
            row.put("xdlvstatus", 1);
            row.put("xdocsite", d.getSite());
            row.put("xacteta", d.getArrivalTime());
            row.put("xactetd", d.getDepartureTime());
            row.put("xcalcdis", 0);
            row.put("xactseq", 0);
            out.add(row);
        }
        return out;
    }

    // ── LVS Header — xr_lvsheader ─────────────────────────────
    @GetMapping("/loadvehstk")
    public Map<String, Object> getLvs(@RequestParam String vrcode) {
        Optional<LvsHeader> found = lvsHeaderRepo.findByTripCode(vrcode);
        if (found.isEmpty()) return Collections.emptyMap();
        LvsHeader h = found.get();

        Map<String, Object> lvs = new LinkedHashMap<>();
        lvs.put("vcrnum", h.getLvsNumber());
        lvs.put("xvrsel", h.getTripCode());
        lvs.put("stofcy", h.getSite());
        lvs.put("salfcy", h.getSite());
        lvs.put("driverid", h.getDriverId());
        lvs.put("codeyve", h.getVehicleCode());
        lvs.put("licplate", h.getVehicleCode());
        lvs.put("dpedat", h.getDepartureDate());
        lvs.put("etd", h.getDepartureTime());
        lvs.put("arvdat", h.getArrivalDate());
        lvs.put("eta", h.getArrivalTime());
        lvs.put("iptdat", h.getDocDate());
        lvs.put("xvrdate", h.getDocDate());
        lvs.put("xloadflg", h.getLoadFlag());
        // BUG FIX: was hardcoded to 1 regardless of actual state.
        // xvalflg now reflects whether LVS Confirm has actually
        // succeeded (confirmed_flag), and confirmedFlag/loadFlag are
        // exposed under clearer names too for the frontend to gate
        // "LVS Confirm" (needs !confirmed) and "Load Truck" (needs
        // confirmed && !loaded) button states correctly.
        lvs.put("xvalflg", h.getConfirmedFlag());
        lvs.put("confirmedFlag", h.getConfirmedFlag());
        lvs.put("loadFlag", h.getLoadFlag());
        lvs.put("xcapacities", h.getCapacityWeight());
        lvs.put("xvehvol", 0.0);
        lvs.put("xroutnbr", 0);
        lvs.put("xtrip", 0);
        lvs.put("xdesfcy", h.getArrSite());
        lvs.put("xbptnum", "");
        lvs.put("credat", h.getCreatedAt());
        lvs.put("creusr", h.getCreatedBy());
        lvs.put("xappusr", h.getDriverId());
        lvs.put("xstatus", 1);

        // Enrich driver name
        String driverId = h.getDriverId();
        if (driverId != null) {
            driverRepo.findById(driverId).ifPresent(d -> lvs.put("driverName", d.getDriverName()));
        }

        // Enrich vehicle class
        String vehicleCode = h.getVehicleCode();
        if (vehicleCode != null) {
            vehicleRepo.findById(vehicleCode).ifPresent(v -> {
                if (v.getCategory() != null) {
                    lvs.put("vehicleClass", v.getCategory().getCategoryCode());
                    lvs.put("vehclass",     v.getCategory().getCategoryCode());
                }
            });
        }

        return lvs;
    }
}
