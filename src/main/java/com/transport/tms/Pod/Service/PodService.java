package com.transport.tms.Pod.Service;

import com.transport.tms.Pod.Dto.*;

import java.time.LocalDate;
import java.util.List;

public interface PodService {

    List<PodTripSummaryDTO> getMyTrips(LocalDate date);

    List<PodStopSummaryDTO> getTripStops(String tripCode);

    PodStopDetailDTO getStopDetail(String docNum);

    PodResponseDTO completeStop(String docNum, PodCompleteRequestDTO req);

    PodResponseDTO getPod(String docNum);
}
