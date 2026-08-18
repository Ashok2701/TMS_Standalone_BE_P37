package com.transport.tms.Pod.Controller;

import com.transport.tms.Pod.Dto.PodTripListItemDTO;
import com.transport.tms.Pod.Impl.PodTripListService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Mobile app trip list — GET /api/pod/trips, protected by the existing
 * /api/pod/** JWT requirement (SecurityConfig). Kept separate from the
 * older, still-disabled PodController (a broader draft for the full
 * trip/stop/POD-submission flow that hasn't been finalized) so this
 * confirmed, in-scope piece can go live on its own.
 */
@RestController
@RequestMapping("/api/pod")
@RequiredArgsConstructor
@Slf4j
public class PodTripListController {

    private final PodTripListService tripListService;

    @GetMapping("/trips")
    public ResponseEntity<Object> getMyTrips() {
        try {
            return ResponseEntity.ok(tripListService.getMyTrips());
        } catch (RuntimeException ex) {
            log.error("Trip list failed: {}", ex.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}
