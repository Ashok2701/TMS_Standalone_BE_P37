package com.transport.tms.Trip.Service;

import com.transport.tms.Trip.Dto.*;
import java.time.LocalDate;
import java.util.List;

public interface TripService {
    TripResponseDTO createTrip(TripRequestDTO dto);
    List<TripResponseDTO> getTripsBySiteAndDate(String site, LocalDate docDate);
    List<TripResponseDTO> getTripsBySite(String site);
    TripResponseDTO getTripById(String tripCode);
    TripResponseDTO updateTrip(String tripCode, TripRequestDTO dto);
    TripResponseDTO updateStatus(String tripCode, TripStatusDTO dto);
    TripResponseDTO optimiseTrip(String tripCode, OptimisationRequestDTO request);
    void deleteTrip(String tripCode);
}
