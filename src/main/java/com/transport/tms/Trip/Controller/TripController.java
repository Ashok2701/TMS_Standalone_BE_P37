package com.transport.tms.Trip.Controller;

import com.transport.tms.Trip.Dto.TripRequestDTO;
import com.transport.tms.Trip.Dto.TripResponseDTO;
import com.transport.tms.Trip.Dto.TripStatusDTO;
import com.transport.tms.Trip.Dto.OptimisationRequestDTO;
import com.transport.tms.Trip.Service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

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
    @GetMapping("/{tripCode}")
    public ResponseEntity<TripResponseDTO> getTripById(@PathVariable String tripCode) {
        return ResponseEntity.ok(service.getTripById(tripCode));
    }

    /** PUT /api/v1/trips/{id} — full update */
    @PutMapping("/{tripCode}")
    public ResponseEntity<TripResponseDTO> updateTrip(
            @PathVariable String tripCode,
            @RequestBody TripRequestDTO request) {
        return ResponseEntity.ok(service.updateTrip(id, request));
    }

    /** PATCH /api/v1/trips/{id}/status — lock / validate / open */
    @PatchMapping("/{tripCode}/status")
    public ResponseEntity<TripResponseDTO> updateStatus(
            @PathVariable String tripCode,
            @RequestBody TripStatusDTO statusDTO) {
        return ResponseEntity.ok(service.updateStatus(id, statusDTO));
    }

    /**
     * PATCH /api/v1/trips/{id}/optimise — run optimisation and persist results
     *
     * Body includes:
     *   - orderMode, startTime, endTime, travelTime, totalDistance, costs…
     *   - stopResults[]: per-stop arrival/departure times + fromPrevDistance/TravelTime
     *
     * Service merges stopResults into each stop in stopObjects JSONB by docNum match.
     */
    @PatchMapping("/{tripCode}/optimise")
    public ResponseEntity<TripResponseDTO> optimiseTrip(
            @PathVariable String tripCode,
            @RequestBody OptimisationRequestDTO request) {
        return ResponseEntity.ok(service.optimiseTrip(tripCode, request));
    }

    /** DELETE /api/v1/trips/{id} */
    @DeleteMapping("/{tripCode}")
    public ResponseEntity<Void> deleteTrip(@PathVariable String tripCode) {
        service.deleteTrip(tripCode);
        return ResponseEntity.noContent().build();
    }
}
