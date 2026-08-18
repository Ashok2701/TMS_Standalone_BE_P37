package com.transport.tms.Pod.Impl;

import com.transport.tms.Fleet.Entity.Driver;
import com.transport.tms.Pod.Dto.PodTripListItemDTO;
import com.transport.tms.Pod.Service.CurrentDriver;
import com.transport.tms.Trip.Lock.Entity.LvsHeader;
import com.transport.tms.Trip.Lock.Repository.LvsHeaderRepository;
import com.transport.tms.Trip.Lock.Repository.VrDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Mobile app "Vehicle Routes List" (Trip List) — source table is
 * xr_lvsheader (replaces X3's XX10CLODSTOH, per the earlier migration),
 * filtered to the authenticated driver via CurrentDriver (never a
 * client-supplied driverId). No date/status filter yet, per current
 * scope — everything assigned to the driver, to be refined later.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PodTripListService {

    private final LvsHeaderRepository lvsHeaderRepository;
    private final VrDetailRepository vrDetailRepository;
    private final CurrentDriver currentDriver;

    public List<PodTripListItemDTO> getMyTrips() {
        Driver driver = currentDriver.require();

        List<LvsHeader> trips = lvsHeaderRepository.findByDriverIdOrderByDocDateDesc(driver.getDriverId());

        return trips.stream()
                .map(lvs -> PodTripListItemDTO.builder()
                        .lvsNumber(lvs.getLvsNumber())
                        .tripNumber(lvs.getTripCode())
                        .driverId(lvs.getDriverId())
                        .date(lvs.getDocDate())
                        .documentCount(vrDetailRepository.countByTripCode(lvs.getTripCode()))
                        .build())
                .toList();
    }
}
