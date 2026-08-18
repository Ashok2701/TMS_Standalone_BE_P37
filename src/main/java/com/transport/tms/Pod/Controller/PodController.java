package com.transport.tms.Pod.Controller;

import com.transport.tms.Pod.Dto.*;
import com.transport.tms.Pod.Service.PodService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// DISABLED for now, per instruction — only driver login is needed at
// this stage, not the trip/stop/POD-submission data endpoints. This
// isn't required for login to work (login has zero dependency on
// PodRepository/xr_pod at all — see DriverAuthServiceImpl), but keeping
// this surface off until the xr_pod table is actually ready avoids
// exposing endpoints that would 500 on first real use ("relation
// xr_pod does not exist"). Re-enable by uncommenting @RestController
// once the table (see POD_API_DOCUMENTATION.md's migration SQL) exists.
//
// NOTE: GET /trips (below, PodService.getMyTrips) is now SUPERSEDED by
// PodTripListController — that one reads from xr_lvsheader per the
// confirmed mobile app spec; this one read from xr_trip.stop_objects,
// an earlier draft design. Removed here to avoid a route collision if
// this controller is ever re-enabled; getStopDetail/completeStop/getPod
// below are unrelated to trip listing and still valid future work.
// @RestController
@RequestMapping("/api/pod")
@RequiredArgsConstructor
@Slf4j
public class PodController {

    private final PodService podService;

    @GetMapping("/stops/{docNum}")
    public ResponseEntity<Object> getStopDetail(@PathVariable String docNum) {
        return ok(() -> podService.getStopDetail(docNum));
    }

    @PostMapping("/stops/{docNum}/complete")
    public ResponseEntity<Object> completeStop(
            @PathVariable String docNum,
            @RequestBody PodCompleteRequestDTO req) {
        return ok(() -> podService.completeStop(docNum, req));
    }

    @GetMapping("/stops/{docNum}/pod")
    public ResponseEntity<Object> getPod(@PathVariable String docNum) {
        return ok(() -> podService.getPod(docNum));
    }

    // Every endpoint follows the same error shape: { "message": "..." }
    // with HTTP 400 on failure (not found / not yours / bad input),
    // matching the pattern already used across the rest of this backend
    // (RoleServiceImpl, XRAuthServiceImpl, etc.) — proper, readable
    // messages the POD team can surface directly, not stack traces.
    private ResponseEntity<Object> ok(java.util.function.Supplier<Object> body) {
        try {
            return ResponseEntity.ok(body.get());
        } catch (RuntimeException ex) {
            log.warn("POD request failed: {}", ex.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}
