package com.transport.tms.Reports.Controller;

import com.transport.tms.Reports.Dto.KpiTransportationDTO;
import com.transport.tms.Reports.Dto.KpiTransportationFilterDTO;
import com.transport.tms.Reports.Service.KpiTransportationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports/kpi-transportation")
@RequiredArgsConstructor
public class KpiTransportationController {

    private final KpiTransportationService kpiTransportationService;

    // GET /api/reports/kpi-transportation
    // Accepts EITHER a JSON body (Axios, Postman, etc.) OR query params
    // (fetch(), plain browser calls). Body wins if the client sent one;
    // otherwise falls back to whatever was bound from query params.
    // Both resolve to the same KpiTransportationFilterDTO, so the service never
    // needs to know which path the request came in on.
    @GetMapping
    public ResponseEntity<List<KpiTransportationDTO>> getKpiTransportations(
            @RequestBody(required = false) KpiTransportationFilterDTO body,
            @ModelAttribute KpiTransportationFilterDTO queryParams) {
        KpiTransportationFilterDTO filter = (body != null) ? body : queryParams;
        return ResponseEntity.ok(kpiTransportationService.getKpiTransportations(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<KpiTransportationDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(kpiTransportationService.getById(id));
    }
}
