package com.transport.tms.Trip.Lock;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Lock / Validate / Unlock endpoints for TMS trips
 *
 * Single:
 *   POST /api/v1/trips/{id}/lock
 *   POST /api/v1/trips/{id}/validate
 *   POST /api/v1/trips/{id}/unlock
 *
 * Group (multiple trips at once):
 *   POST /api/v1/trips/group/lock
 *   POST /api/v1/trips/group/validate
 *   POST /api/v1/trips/group/unlock
 */
@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
public class TripLockController {

    private final TripLockService lockService;

    // ── Single trip ───────────────────────────────────────────

    @PostMapping("/{id}/lock")
    public ResponseEntity<?> lock(
            @PathVariable Long id,
            @RequestParam(defaultValue = "SYSTEM") String userCode) {
        lockService.lockTrip(id, userCode);
        return ResponseEntity.ok(Map.of(
            "message", "Trip locked successfully",
            "tripId", id,
            "action", "LOCK"
        ));
    }

    @PostMapping("/{id}/validate")
    public ResponseEntity<?> validate(
            @PathVariable Long id,
            @RequestParam(defaultValue = "SYSTEM") String userCode) {
        lockService.validateTrip(id, userCode);
        return ResponseEntity.ok(Map.of(
            "message", "Trip validated successfully — LVS created in X3",
            "tripId", id,
            "action", "VALIDATE"
        ));
    }

    @PostMapping("/{id}/unlock")
    public ResponseEntity<?> unlock(
            @PathVariable Long id,
            @RequestParam(defaultValue = "SYSTEM") String userCode) {
        lockService.unlockTrip(id, userCode);
        return ResponseEntity.ok(Map.of(
            "message", "Trip unlocked successfully",
            "tripId", id,
            "action", "UNLOCK"
        ));
    }

    // ── Group actions ─────────────────────────────────────────

    @PostMapping("/group/lock")
    public ResponseEntity<?> groupLock(
            @RequestBody List<Long> tripIds,
            @RequestParam(defaultValue = "SYSTEM") String userCode) {
        lockService.lockTrips(tripIds, userCode);
        return ResponseEntity.ok(Map.of(
            "message", tripIds.size() + " trip(s) locked",
            "count", tripIds.size(),
            "action", "GROUP_LOCK"
        ));
    }

    @PostMapping("/group/validate")
    public ResponseEntity<?> groupValidate(
            @RequestBody List<Long> tripIds,
            @RequestParam(defaultValue = "SYSTEM") String userCode) {
        lockService.validateTrips(tripIds, userCode);
        return ResponseEntity.ok(Map.of(
            "message", tripIds.size() + " trip(s) validated",
            "count", tripIds.size(),
            "action", "GROUP_VALIDATE"
        ));
    }

    @PostMapping("/group/unlock")
    public ResponseEntity<?> groupUnlock(
            @RequestBody List<Long> tripIds,
            @RequestParam(defaultValue = "SYSTEM") String userCode) {
        for (Long id : tripIds) {
            try { lockService.unlockTrip(id, userCode); }
            catch (Exception e) { /* log and continue */ }
        }
        return ResponseEntity.ok(Map.of(
            "message", tripIds.size() + " trip(s) unlocked",
            "count", tripIds.size(),
            "action", "GROUP_UNLOCK"
        ));
    }
}
