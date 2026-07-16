package com.transport.tms.Reports.Controller;

import com.transport.tms.Reports.Dto.VehicleReportDTO;
import com.transport.tms.Reports.Dto.VehicleReportFilterDTO;
import com.transport.tms.Reports.Service.VehicleReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports/vehiclereport")
@RequiredArgsConstructor
public class VehicleReportController {

    private final VehicleReportService vehicleReportService;

    @GetMapping
    public ResponseEntity<List<VehicleReportDTO>> getVehicleReport(
            @RequestBody(required = false) VehicleReportFilterDTO body,
            @ModelAttribute VehicleReportFilterDTO queryParams) {
        VehicleReportFilterDTO filter = (body != null) ? body : queryParams;
        return ResponseEntity.ok(vehicleReportService.getVehicleReport(filter));
    }
}