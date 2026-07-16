package com.transport.tms.Reports.Impl;

import com.transport.tms.Reports.Dto.KpiTransportationDTO;
import com.transport.tms.Reports.Dto.KpiTransportationFilterDTO;
import com.transport.tms.Reports.Entity.KpiTransportationEntity;
import com.transport.tms.Reports.Repository.KpiTransportationRepository;
import com.transport.tms.Reports.Service.KpiTransportationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KpiTransportationServiceImpl implements KpiTransportationService {

    private final KpiTransportationRepository kpiTransportationRepository;

    @Override
    public List<KpiTransportationDTO> getKpiTransportations(KpiTransportationFilterDTO filter) {
        // TODO: query kpiTransportationRepository using filter, map entities -> DTOs
        return null;
    }

    @Override
    public KpiTransportationDTO getById(Long id) {
        // TODO: fetch by id, map entity -> DTO, throw not-found if missing
        return null;
    }
}
