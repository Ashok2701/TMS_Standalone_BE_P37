package com.transport.tms.Reports.Controller;

import com.transport.tms.Reports.Dto.DriverReportDTO;
import com.transport.tms.Reports.Dto.DriverReportFilterDTO;
import com.transport.tms.Reports.Service.DriverReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports/drivers")
@RequiredArgsConstructor
public class DriverReportController {

    private final DriverReportService driverReportService;

    // GET /api/reports/drivers
    // Accepts EITHER a JSON body (Axios, Postman, etc.) OR query params
    // (fetch(), plain browser calls). Body wins if the client sent one;
    // otherwise falls back to whatever was bound from query params.
    // Both resolve to the same DriverReportFilterDTO, so the service never
    // needs to know which path the request came in on.
    @GetMapping
    public ResponseEntity<List<DriverReportDTO>> getDriverReports(
            @RequestBody(required = false) DriverReportFilterDTO body,
            @ModelAttribute DriverReportFilterDTO queryParams) {
        DriverReportFilterDTO filter = (body != null) ? body : queryParams;
        return ResponseEntity.ok(driverReportService.getDriverReports(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DriverReportDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(driverReportService.getById(id));
    }
}
