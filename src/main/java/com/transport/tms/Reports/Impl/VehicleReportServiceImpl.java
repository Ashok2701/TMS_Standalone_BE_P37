package com.transport.tms.Reports.Impl;

import com.transport.tms.Reports.Dto.VehicleReportDTO;
import com.transport.tms.Reports.Dto.VehicleReportFilterDTO;
import com.transport.tms.Reports.Repository.VehicleReportRepository;
import com.transport.tms.Reports.Service.VehicleReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleReportServiceImpl implements VehicleReportService {

    private final VehicleReportRepository vehicleReportRepository;

    @Override
    public List<VehicleReportDTO> getVehicleReport(VehicleReportFilterDTO filter) {
        return vehicleReportRepository.findVehicleReport(
                filter.getStartDate(), filter.getEndDate(), filter.getSite());
    }
}