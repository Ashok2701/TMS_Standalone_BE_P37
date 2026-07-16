package com.transport.tms.Reports.Service;

import com.transport.tms.Reports.Dto.PodTrackingDTO;
import com.transport.tms.Reports.Dto.PodTrackingFilterDTO;

import java.util.List;

public interface PodTrackingService {

    List<PodTrackingDTO> getPodTrackings(PodTrackingFilterDTO filter);

    PodTrackingDTO getById(Long id);
}
