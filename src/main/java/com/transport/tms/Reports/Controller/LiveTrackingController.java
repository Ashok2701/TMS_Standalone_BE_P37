package com.transport.tms.Reports.Controller;

import com.transport.tms.Reports.Dto.LiveTrackingDTO;
import com.transport.tms.Reports.Dto.LiveTrackingFilterDTO;
import com.transport.tms.Reports.Service.LiveTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports/live-tracking")
@RequiredArgsConstructor
public class LiveTrackingController {

    private final LiveTrackingService liveTrackingService;

    // GET /api/reports/live-tracking
    // Accepts EITHER a JSON body (Axios, Postman, etc.) OR query params
    // (fetch(), plain browser calls). Body wins if the client sent one;
    // otherwise falls back to whatever was bound from query params.
    // Both resolve to the same LiveTrackingFilterDTO, so the service never
    // needs to know which path the request came in on.
    @GetMapping
    public ResponseEntity<List<LiveTrackingDTO>> getLiveTrackings(
            @RequestBody(required = false) LiveTrackingFilterDTO body,
            @ModelAttribute LiveTrackingFilterDTO queryParams) {
        LiveTrackingFilterDTO filter = (body != null) ? body : queryParams;
        return ResponseEntity.ok(liveTrackingService.getLiveTrackings(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LiveTrackingDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(liveTrackingService.getById(id));
    }
}
