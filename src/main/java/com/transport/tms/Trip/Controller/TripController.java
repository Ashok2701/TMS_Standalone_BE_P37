package com.transport.tms.Trip.Controller;

import com.transport.tms.Trip.Dto.TripRequestDTO;
import com.transport.tms.Trip.Dto.TripResponseDTO;
import com.transport.tms.Trip.Dto.TripStatusDTO;
import com.transport.tms.Trip.Service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TripController {

    private final TripService service;

    /** POST /api/v1/trips — confirm a new trip */
    @PostMapping
    public ResponseEntity<TripResponseDTO> createTrip(@RequestBody TripRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createTrip(request));
    }

    /** GET /api/v1/trips?site=X&date=YYYY-MM-DD */
    @GetMapping
    public ResponseEntity<List<TripResponseDTO>> getTrips(
            @RequestParam String site,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date != null) {
            return ResponseEntity.ok(service.getTripsBySiteAndDate(site, date));
        }
        return ResponseEntity.ok(service.getTripsBySite(site));
    }

    /** GET /api/v1/trips/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<TripResponseDTO> getTripById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getTripById(id));
    }

    /** PUT /api/v1/trips/{id} — full update */
    @PutMapping("/{id}")
    public ResponseEntity<TripResponseDTO> updateTrip(
            @PathVariable Long id,
            @RequestBody TripRequestDTO request) {
        return ResponseEntity.ok(service.updateTrip(id, request));
    }

    /** PATCH /api/v1/trips/{id}/status — lock / validate / open */
    @PatchMapping("/{id}/status")
    public ResponseEntity<TripResponseDTO> updateStatus(
            @PathVariable Long id,
            @RequestBody TripStatusDTO statusDTO) {
        return ResponseEntity.ok(service.updateStatus(id, statusDTO));
    }

    /** PATCH /api/v1/trips/{id}/optimise — run optimisation */
    @PatchMapping("/{id}/optimise")
    public ResponseEntity<TripResponseDTO> optimiseTrip(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String orderMode = body.getOrDefault("orderMode", "fixed");
        String startTime = body.get("startTime");
        return ResponseEntity.ok(service.optimiseTrip(id, orderMode, startTime));
    }

    /** DELETE /api/v1/trips/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrip(@PathVariable Long id) {
        service.deleteTrip(id);
        return ResponseEntity.noContent().build();
    }
}
