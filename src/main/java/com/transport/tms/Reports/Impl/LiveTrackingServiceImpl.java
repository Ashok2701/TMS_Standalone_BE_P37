package com.transport.tms.Reports.Impl;

import com.transport.tms.Reports.Dto.LiveTrackingDTO;
import com.transport.tms.Reports.Dto.LiveTrackingFilterDTO;
import com.transport.tms.Reports.Entity.LiveTrackingEntity;
import com.transport.tms.Reports.Repository.LiveTrackingRepository;
import com.transport.tms.Reports.Service.LiveTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LiveTrackingServiceImpl implements LiveTrackingService {

    private final LiveTrackingRepository liveTrackingRepository;

    @Override
    public List<LiveTrackingDTO> getLiveTrackings(LiveTrackingFilterDTO filter) {
        // TODO: query liveTrackingRepository using filter, map entities -> DTOs
        return null;
    }

    @Override
    public LiveTrackingDTO getById(Long id) {
        // TODO: fetch by id, map entity -> DTO, throw not-found if missing
        return null;
    }
}
