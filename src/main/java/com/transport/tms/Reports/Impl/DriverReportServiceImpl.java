package com.transport.tms.Reports.Impl;

import com.transport.tms.Reports.Dto.DriverReportDTO;
import com.transport.tms.Reports.Dto.DriverReportFilterDTO;
import com.transport.tms.Reports.Entity.DriverReportEntity;
import com.transport.tms.Reports.Repository.DriverReportRepository;
import com.transport.tms.Reports.Service.DriverReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DriverReportServiceImpl implements DriverReportService {

    private final DriverReportRepository driverReportRepository;

    @Override
    public List<DriverReportDTO> getDriverReports(DriverReportFilterDTO filter) {
        // TODO: query driverReportRepository using filter, map entities -> DTOs
        return null;
    }

    @Override
    public DriverReportDTO getById(Long id) {
        // TODO: fetch by id, map entity -> DTO, throw not-found if missing
        return null;
    }
}
