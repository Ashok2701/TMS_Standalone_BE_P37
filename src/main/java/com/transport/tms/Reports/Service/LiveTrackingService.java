package com.transport.tms.Reports.Service;

import com.transport.tms.Reports.Dto.LiveTrackingDTO;
import com.transport.tms.Reports.Dto.LiveTrackingFilterDTO;

import java.util.List;

public interface LiveTrackingService {

    List<LiveTrackingDTO> getLiveTrackings(LiveTrackingFilterDTO filter);

    LiveTrackingDTO getById(Long id);
}
