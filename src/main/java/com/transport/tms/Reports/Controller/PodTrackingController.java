package com.transport.tms.Reports.Controller;

import com.transport.tms.Reports.Dto.PodTrackingDTO;
import com.transport.tms.Reports.Dto.PodTrackingFilterDTO;
import com.transport.tms.Reports.Service.PodTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports/pod-tracking")
@RequiredArgsConstructor
public class PodTrackingController {

    private final PodTrackingService podTrackingService;

    // GET /api/reports/pod-tracking
    // Accepts EITHER a JSON body (Axios, Postman, etc.) OR query params
    // (fetch(), plain browser calls). Body wins if the client sent one;
    // otherwise falls back to whatever was bound from query params.
    // Both resolve to the same PodTrackingFilterDTO, so the service never
    // needs to know which path the request came in on.
    @GetMapping
    public ResponseEntity<List<PodTrackingDTO>> getPodTrackings(
            @RequestBody(required = false) PodTrackingFilterDTO body,
            @ModelAttribute PodTrackingFilterDTO queryParams) {
        PodTrackingFilterDTO filter = (body != null) ? body : queryParams;
        return ResponseEntity.ok(podTrackingService.getPodTrackings(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PodTrackingDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(podTrackingService.getById(id));
    }
}
