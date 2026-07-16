package com.transport.tms.Reports.Impl;

import com.transport.tms.Reports.Dto.PodTrackingDTO;
import com.transport.tms.Reports.Dto.PodTrackingFilterDTO;
import com.transport.tms.Reports.Entity.PodTrackingEntity;
import com.transport.tms.Reports.Repository.PodTrackingRepository;
import com.transport.tms.Reports.Service.PodTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PodTrackingServiceImpl implements PodTrackingService {

    private final PodTrackingRepository podTrackingRepository;

    @Override
    public List<PodTrackingDTO> getPodTrackings(PodTrackingFilterDTO filter) {
        // TODO: query podTrackingRepository using filter, map entities -> DTOs
        return null;
    }

    @Override
    public PodTrackingDTO getById(Long id) {
        // TODO: fetch by id, map entity -> DTO, throw not-found if missing
        return null;
    }
}
