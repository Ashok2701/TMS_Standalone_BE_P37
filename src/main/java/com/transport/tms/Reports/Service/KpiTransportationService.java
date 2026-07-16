package com.transport.tms.Reports.Service;

import com.transport.tms.Reports.Dto.KpiTransportationDTO;
import com.transport.tms.Reports.Dto.KpiTransportationFilterDTO;

import java.util.List;

public interface KpiTransportationService {

    List<KpiTransportationDTO> getKpiTransportations(KpiTransportationFilterDTO filter);

    KpiTransportationDTO getById(Long id);
}
