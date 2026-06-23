package com.transport.tms.Trip.Service;

import com.transport.tms.Trip.Dto.TripRequestDTO;
import com.transport.tms.Trip.Dto.TripResponseDTO;
import com.transport.tms.Trip.Dto.TripStatusDTO;
import java.time.LocalDate;
import java.util.List;

public interface TripService {
    TripResponseDTO createTrip(TripRequestDTO request);
    List<TripResponseDTO> getTripsBySiteAndDate(String site, LocalDate docDate);
    List<TripResponseDTO> getTripsBySite(String site);
    TripResponseDTO getTripById(Long id);
    TripResponseDTO updateTrip(Long id, TripRequestDTO request);
    TripResponseDTO updateStatus(Long id, TripStatusDTO statusDTO);
    TripResponseDTO optimiseTrip(Long id, String orderMode, String startTime);
    void deleteTrip(Long id);
}
