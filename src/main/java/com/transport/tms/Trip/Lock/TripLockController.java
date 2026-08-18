package com.transport.tms.Trip.Lock;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Lock / Validate / Unlock using tripCode (e.g. VR-KCC01-20260624-001)
 *
 * Single:
 *   POST /api/v1/trips/{tripCode}/lock
 *   POST /api/v1/trips/{tripCode}/validate
 *   POST /api/v1/trips/{tripCode}/unlock
 *
 * Group:
 *   POST /api/v1/trips/group/lock      body: ["VR-KCC01-001","VR-KCC01-002"]
 *   POST /api/v1/trips/group/validate  body: ["VR-KCC01-001"]
 *   POST /api/v1/trips/group/unlock    body: ["VR-KCC01-001"]
 */
@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
public class TripLockController {

    private final TripLockService lockService;

    @PostMapping("/{tripCode}/lock")
    public ResponseEntity<?> lock(
            @PathVariable String tripCode,
            @RequestParam(defaultValue = "SYSTEM") String userCode) {
        lockService.lockTrip(tripCode, userCode);
        return ResponseEntity.ok(Map.of(
            "message", "Trip locked", "tripCode", tripCode, "action", "LOCK"));
    }

    @PostMapping("/{tripCode}/validate")
    public ResponseEntity<?> validate(
            @PathVariable String tripCode,
            @RequestParam(defaultValue = "SYSTEM") String userCode) {
        lockService.validateTrip(tripCode, userCode);
        return ResponseEntity.ok(Map.of(
            "message", "Trip validated — LVS created", "tripCode", tripCode, "action", "VALIDATE"));
    }

    /** LVS Confirm — XX10CRESDH for every document on the trip, then
     *  sets xr_lvsheader.confirmed_flag on success. */
    @PostMapping("/{tripCode}/lvs-confirm")
    public ResponseEntity<?> lvsConfirm(@PathVariable String tripCode) {
        Map<String, Object> x3Response = lockService.confirmLvs(tripCode);
        return ResponseEntity.ok(Map.of(
            "message", "LVS confirmed", "tripCode", tripCode, "action", "LVS_CONFIRM", "x3Response", x3Response));
    }

    /** Load Truck — X10CSTKMTV, then sets xr_lvsheader.load_flag on
     *  success. Blocked server-side unless confirmed_flag is already set. */
    @PostMapping("/{tripCode}/load-truck")
    public ResponseEntity<?> loadTruck(@PathVariable String tripCode) {
        Map<String, Object> x3Response = lockService.loadTruck(tripCode);
        return ResponseEntity.ok(Map.of(
            "message", "Truck loaded", "tripCode", tripCode, "action", "LOAD_TRUCK", "x3Response", x3Response));
    }

    @PostMapping("/{tripCode}/unlock")
    public ResponseEntity<?> unlock(
            @PathVariable String tripCode,
            @RequestParam(defaultValue = "SYSTEM") String userCode) {
        lockService.unlockTrip(tripCode, userCode);
        return ResponseEntity.ok(Map.of(
            "message", "Trip unlocked", "tripCode", tripCode, "action", "UNLOCK"));
    }

    @PostMapping("/group/lock")
    public ResponseEntity<?> groupLock(
            @RequestBody List<String> tripCodes,
            @RequestParam(defaultValue = "SYSTEM") String userCode) {
        lockService.lockTrips(tripCodes, userCode);
        return ResponseEntity.ok(Map.of("message", tripCodes.size() + " trip(s) locked", "action", "GROUP_LOCK"));
    }

    @PostMapping("/group/validate")
    public ResponseEntity<?> groupValidate(
            @RequestBody List<String> tripCodes,
            @RequestParam(defaultValue = "SYSTEM") String userCode) {
        lockService.validateTrips(tripCodes, userCode);
        return ResponseEntity.ok(Map.of("message", tripCodes.size() + " trip(s) validated", "action", "GROUP_VALIDATE"));
    }

    @PostMapping("/group/unlock")
    public ResponseEntity<?> groupUnlock(
            @RequestBody List<String> tripCodes,
            @RequestParam(defaultValue = "SYSTEM") String userCode) {
        lockService.unlockTrips(tripCodes, userCode);
        return ResponseEntity.ok(Map.of("message", tripCodes.size() + " trip(s) unlocked", "action", "GROUP_UNLOCK"));
    }
}
