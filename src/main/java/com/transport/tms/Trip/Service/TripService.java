package com.transport.tms.Trip.Service;

import com.transport.tms.Trip.Dto.TripRequestDTO;
import com.transport.tms.Trip.Dto.TripResponseDTO;
import com.transport.tms.Trip.Dto.TripStatusDTO;
import com.transport.tms.Trip.Dto.OptimisationRequestDTO;
import java.time.LocalDate;
import java.util.List;

public interface TripService {
    TripResponseDTO createTrip(TripRequestDTO request);
    List<TripResponseDTO> getTripsBySiteAndDate(String site, LocalDate docDate);
    List<TripResponseDTO> getTripsBySite(String site);
    TripResponseDTO getTripById(String tripCode);
    TripResponseDTO updateTrip(Long id, TripRequestDTO request);
    TripResponseDTO updateStatus(Long id, TripStatusDTO statusDTO);
    TripResponseDTO optimiseTrip(String tripCode, OptimisationRequestDTO request);
    void deleteTrip(String tripCode);
}
