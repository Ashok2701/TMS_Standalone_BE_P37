package com.transport.tms.Trip.Impl;

import com.transport.tms.Trip.Dto.TripRequestDTO;
import com.transport.tms.Trip.Dto.TripResponseDTO;
import com.transport.tms.Trip.Dto.TripStatusDTO;
import com.transport.tms.Trip.Entity.XrTrip;
import com.transport.tms.Trip.Repository.TripRepository;
import com.transport.tms.Trip.Service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TripServiceImpl implements TripService {

    private final TripRepository repo;

    // ── CREATE ────────────────────────────────────────────────
    @Override
    public TripResponseDTO createTrip(TripRequestDTO req) {
        XrTrip trip = new XrTrip();
        mapRequestToEntity(req, trip);

        // Auto-generate trip_code: XVR-YYYYMMDD-SITE-SEQ
        int nextSeq = repo.findMaxStartIndex(req.getSite(), req.getDocDate()) + 1;
        String date = req.getDocDate().format(DateTimeFormatter.BASIC_ISO_DATE);
        String code = String.format("XVR-%s-%s-%03d", date, req.getSite(), nextSeq);
        trip.setTripCode(code);
        trip.setStartIndex(nextSeq);
        trip.setStops((req.getDrops() == null ? 0 : req.getDrops())
                    + (req.getPickups() == null ? 0 : req.getPickups()));
        trip.setOptiStatus("Open");

        return toDTO(repo.save(trip));
    }

    // ── READ ALL by site + date ───────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<TripResponseDTO> getTripsBySiteAndDate(String site, LocalDate docDate) {
        return repo.findBySiteAndDocDateOrderByCreateDateAsc(site, docDate)
                   .stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ── READ ALL by site ──────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<TripResponseDTO> getTripsBySite(String site) {
        return repo.findBySiteOrderByDocDateDescCreateDateAsc(site)
                   .stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ── READ ONE ──────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public TripResponseDTO getTripById(Long id) {
        return toDTO(findOrThrow(id));
    }

    // ── UPDATE full ───────────────────────────────────────────
    @Override
    public TripResponseDTO updateTrip(Long id, TripRequestDTO req) {
        XrTrip trip = findOrThrow(id);
        mapRequestToEntity(req, trip);
        trip.setStops((req.getDrops() == null ? 0 : req.getDrops())
                    + (req.getPickups() == null ? 0 : req.getPickups()));
        return toDTO(repo.save(trip));
    }

    // ── PATCH status (lock / validate / open) ────────────────
    @Override
    public TripResponseDTO updateStatus(Long id, TripStatusDTO dto) {
        XrTrip trip = findOrThrow(id);
        if (dto.getOptiStatus() != null) trip.setOptiStatus(dto.getOptiStatus());
        if (dto.getLockFlag()   != null) trip.setLockFlag(dto.getLockFlag());
        if (dto.getNotes()      != null) trip.setNotes(dto.getNotes());
        if (dto.getUserCode()   != null) trip.setUserCode(dto.getUserCode());
        return toDTO(repo.save(trip));
    }

    // ── PATCH optimise ────────────────────────────────────────
    @Override
    public TripResponseDTO optimiseTrip(Long id, String orderMode, String startTime) {
        XrTrip trip = findOrThrow(id);
        trip.setOptiStatus("Optimised");
        trip.setHeuExec(orderMode != null ? orderMode : "fixed");
        trip.setDatExec(OffsetDateTime.now());
        if (startTime != null) trip.setStartTime(startTime);
        return toDTO(repo.save(trip));
    }

    // ── DELETE ────────────────────────────────────────────────
    @Override
    public void deleteTrip(Long id) {
        XrTrip trip = findOrThrow(id);
        repo.delete(trip);
    }

    // ── Helpers ───────────────────────────────────────────────
    private XrTrip findOrThrow(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Trip not found: " + id));
    }

    private void mapRequestToEntity(TripRequestDTO req, XrTrip trip) {
        trip.setSite(req.getSite());
        trip.setDocDate(req.getDocDate());
        trip.setDriverId(req.getDriverId());
        trip.setDriverName(req.getDriverName() != null ? req.getDriverName() : "");
        trip.setVehicleCode(req.getVehicleCode());
        trip.setDepSite(req.getDepSite());
        trip.setArrSite(req.getArrSite());
        trip.setDrops(req.getDrops()   != null ? req.getDrops()   : 0);
        trip.setPickups(req.getPickups()!= null ? req.getPickups() : 0);
        trip.setNoOfPackages(req.getNoOfPackages());
        trip.setStartTime(req.getStartTime());
        trip.setEndTime(req.getEndTime());
        trip.setTravelTime(req.getTravelTime());
        trip.setTotalTime(req.getTotalTime());
        trip.setServiceTime(req.getServiceTime());
        trip.setTotalWeight(req.getTotalWeight());
        trip.setTotalVolume(req.getTotalVolume());
        trip.setCapacity(req.getCapacity());
        trip.setUomCapacity(req.getUomCapacity());
        trip.setUomVolume(req.getUomVolume());
        trip.setUomTime(req.getUomTime());
        trip.setUomDistance(req.getUomDistance());
        trip.setWeightPct(req.getWeightPct());
        trip.setVolumePct(req.getVolumePct());
        trip.setTotalDistance(req.getTotalDistance());
        trip.setTotalCost(req.getTotalCost());
        trip.setDistanceCost(req.getDistanceCost());
        trip.setFixedCost(req.getFixedCost());
        trip.setServiceCost(req.getServiceCost());
        trip.setRegularCost(req.getRegularCost());
        trip.setOvertimeCost(req.getOvertimeCost());
        trip.setNotes(req.getNotes());
        trip.setGeneratedBy(req.getGeneratedBy() != null ? req.getGeneratedBy() : "PLANNER");
        trip.setForceSeq(req.getForceSeq() != null ? req.getForceSeq() : 0);
        trip.setVrSeq(req.getVrSeq());
        // stopObjects: array of all stops in order (drops + pickups)
        trip.setStopObjects(req.getStopObjects());
        trip.setVehicleObject(req.getVehicleObject());
        trip.setTotalObject(req.getTotalObject());
        trip.setTotCapacity(req.getTotCapacity());
        trip.setTotVolumeCap(req.getTotVolumeCap());
        trip.setDocCapacity(req.getDocCapacity());
        trip.setDocVolume(req.getDocVolume());
        trip.setPerCapacity(req.getPerCapacity());
        trip.setPerVolume(req.getPerVolume());
        trip.setDocQty(req.getDocQty());
        trip.setUomQty(req.getUomQty());
        trip.setMaxPalletCnt(req.getMaxPalletCnt());
        trip.setUserCode(req.getUserCode() != null ? req.getUserCode() : "SYSTEM");
    }

    private TripResponseDTO toDTO(XrTrip t) {
        TripResponseDTO dto = new TripResponseDTO();
        dto.setTripId(t.getTripId());
        dto.setTripCode(t.getTripCode());
        dto.setSite(t.getSite());
        dto.setDocDate(t.getDocDate());
        dto.setDriverId(t.getDriverId());
        dto.setDriverName(t.getDriverName());
        dto.setVehicleCode(t.getVehicleCode());
        dto.setStops(t.getStops());
        dto.setDrops(t.getDrops());
        dto.setPickups(t.getPickups());
        dto.setNoOfPackages(t.getNoOfPackages());
        dto.setDepSite(t.getDepSite());
        dto.setArrSite(t.getArrSite());
        dto.setStartTime(t.getStartTime());
        dto.setEndTime(t.getEndTime());
        dto.setTravelTime(t.getTravelTime());
        dto.setTotalTime(t.getTotalTime());
        dto.setServiceTime(t.getServiceTime());
        dto.setTotalWeight(t.getTotalWeight());
        dto.setTotalVolume(t.getTotalVolume());
        dto.setCapacity(t.getCapacity());
        dto.setUomCapacity(t.getUomCapacity());
        dto.setUomVolume(t.getUomVolume());
        dto.setUomTime(t.getUomTime());
        dto.setUomDistance(t.getUomDistance());
        dto.setWeightPct(t.getWeightPct());
        dto.setVolumePct(t.getVolumePct());
        dto.setTotalDistance(t.getTotalDistance());
        dto.setTotalCost(t.getTotalCost());
        dto.setDistanceCost(t.getDistanceCost());
        dto.setFixedCost(t.getFixedCost());
        dto.setServiceCost(t.getServiceCost());
        dto.setOptiStatus(t.getOptiStatus());
        dto.setLockFlag(t.getLockFlag());
        dto.setForceSeq(t.getForceSeq());
        dto.setVrSeq(t.getVrSeq());
        dto.setNotes(t.getNotes());
        dto.setGeneratedBy(t.getGeneratedBy());
        dto.setStopObjects(t.getStopObjects());  // List<Object>
        dto.setVehicleObject(t.getVehicleObject());
        dto.setTotalObject(t.getTotalObject());
        dto.setPerCapacity(t.getPerCapacity());
        dto.setPerVolume(t.getPerVolume());
        dto.setDocQty(t.getDocQty());
        dto.setAlertFlag(t.getAlertFlag());
        dto.setWarningNotes(t.getWarningNotes());
        dto.setUserCode(t.getUserCode());
        dto.setCreateDate(t.getCreateDate());
        dto.setUpdateDate(t.getUpdateDate());
        return dto;
    }
}
