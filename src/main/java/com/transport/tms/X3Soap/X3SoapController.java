package com.transport.tms.X3Soap;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST endpoints wrapping Sage X3 SOAP services.
 * Credentials stay server-side — frontend never sees them.
 *
 * All endpoints: GET /api/v1/x3/...
 */
@RestController
@RequestMapping("/api/v1/x3")
@RequiredArgsConstructor
public class X3SoapController {

    private final X3SoapService soapService;

    /** Confirm/validate LVS in X3 — X10CCONBUT */
    @PostMapping("/confirm-lvs")
    public Map<String, Object> confirmLvs(@RequestParam String lvsNum) {
        return soapService.confirmLvs(lvsNum);
    }

    /** Confirm route/trip in X3 — X1CONFIRM (input: I_XNUMPC = VR/trip number) */
    @PostMapping("/confirm-route")
    public Map<String, Object> confirmRoute(@RequestParam String vrNumber) {
        return soapService.confirmRoute(vrNumber);
    }

    /** Get route/trip detail — X1CROUTDET */
    @GetMapping("/route-detail")
    public Map<String, Object> getRouteDetail(@RequestParam String vrNum) {
        return soapService.getRouteDetail(vrNum);
    }

    /** Get allocation details — X1CALLDET */
    @GetMapping("/allocation-details")
    public Map<String, Object> getAllocationDetails(
            @RequestParam String vrNum,
            @RequestParam(defaultValue = "") String floctyp,
            @RequestParam(defaultValue = "") String tloctyp,
            @RequestParam(defaultValue = "") String floc,
            @RequestParam(defaultValue = "") String tloc) {
        return soapService.getAllocationDetails(vrNum, floctyp, tloctyp, floc, tloc);
    }

    /** Submit pick allocation — X1CPICALL */
    @PostMapping("/submit-allocation")
    public Map<String, Object> submitAllocation(@RequestParam String pickNum) {
        return soapService.submitAllocation(pickNum);
    }

    /** Get lot details — X1CLOTDET */
    @GetMapping("/lot-details")
    public Map<String, Object> getLotDetails(
            @RequestParam String site,
            @RequestParam String productNum,
            @RequestParam String vrNum) {
        return soapService.getLotDetails(site, productNum, vrNum);
    }

    /** Get staging location allocation data — X1CSTASTO */
    @GetMapping("/staging-allocation")
    public Map<String, Object> getStagingAllocation(
            @RequestParam String vrNum,
            @RequestParam(defaultValue = "") String fromloc,
            @RequestParam(defaultValue = "") String toloc,
            @RequestParam(defaultValue = "") String floc,
            @RequestParam(defaultValue = "") String tloc) {
        return soapService.getAllocatedDataByStagingLocations(vrNum, fromloc, toloc, floc, tloc);
    }

    /** Get staging locations — X1CSTALOC */
    @GetMapping("/staging-locations")
    public Map<String, Object> getStagingLocations(@RequestParam String site) {
        return soapService.getStagingLocations(site);
    }

    /** Get locations by type — X1CLOCSEL */
    @GetMapping("/locations")
    public Map<String, Object> getLocations(
            @RequestParam String site,
            @RequestParam(defaultValue = "") String floctyp,
            @RequestParam(defaultValue = "") String tloctyp) {
        return soapService.getLocations(site, floctyp, tloctyp);
    }

    /** Delete pick ticket documents — XPCKTCKDL */
    @DeleteMapping("/documents")
    public Map<String, Object> deleteDocuments(@RequestBody List<String> docNums) {
        return soapService.deleteDocuments(docNums);
    }
}
