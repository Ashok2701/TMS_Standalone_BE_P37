package com.transport.tms.Reports.Service;

import com.transport.tms.Reports.Dto.DriverReportDTO;
import com.transport.tms.Reports.Dto.DriverReportFilterDTO;

import java.util.List;

public interface DriverReportService {

    List<DriverReportDTO> getDriverReports(DriverReportFilterDTO filter);

    DriverReportDTO getById(Long id);
}
