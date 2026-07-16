package com.transport.tms.Reports.Service;

import com.transport.tms.Reports.Dto.VehicleReportDTO;
import com.transport.tms.Reports.Dto.VehicleReportFilterDTO;

import java.util.List;

public interface VehicleReportService {
    List<VehicleReportDTO> getVehicleReport(VehicleReportFilterDTO filter);
}