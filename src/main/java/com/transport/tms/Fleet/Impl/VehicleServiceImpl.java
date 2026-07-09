package com.transport.tms.Fleet.Impl;

import com.transport.tms.Fleet.Dto.VehicleDTO;
import com.transport.tms.Fleet.Entity.Vehicle;
import com.transport.tms.Fleet.Entity.VehicleCategory;
import com.transport.tms.Fleet.Repository.VehicleCategoryRepository;
import com.transport.tms.Fleet.Repository.VehicleRepository;
import com.transport.tms.Fleet.Service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository         vehicleRepository;
    private final VehicleCategoryRepository categoryRepository;

    // ── CREATE ────────────────────────────────────────────────
    @Override
    public VehicleDTO create(VehicleDTO dto) {
        if (vehicleRepository.existsByVehicleCode(dto.getVehicleCode())) {
            throw new RuntimeException("Vehicle code already exists: " + dto.getVehicleCode());
        }
        Vehicle entity = new Vehicle();
        entity.setUuid(UUID.randomUUID());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setCreatedBy(dto.getCreatedBy() != null ? dto.getCreatedBy() : "SYSTEM");
        fromDTO(dto, entity);
        return mapToDTO(vehicleRepository.save(entity));
    }

    // ── UPDATE ────────────────────────────────────────────────
    @Override
    public VehicleDTO update(String vehicleCode, VehicleDTO dto) {
        Vehicle entity = vehicleRepository.findById(vehicleCode)
                .orElseThrow(() -> new RuntimeException("Vehicle not found: " + vehicleCode));
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy(dto.getUpdatedBy() != null ? dto.getUpdatedBy() : "SYSTEM");
        fromDTO(dto, entity);
        return mapToDTO(vehicleRepository.save(entity));
    }

    // ── GET BY ID ─────────────────────────────────────────────
    @Override
    public VehicleDTO getById(String vehicleCode) {
        return vehicleRepository.findById(vehicleCode)
                .map(this::mapToDTO)
                .orElseThrow(() -> new RuntimeException("Vehicle not found: " + vehicleCode));
    }

    // ── GET ALL ───────────────────────────────────────────────
    @Override
    public List<VehicleDTO> getAll() {
        return vehicleRepository.findAll().stream()
                .map(this::mapToDTO)
                .toList();
    }

    // ── DTO → Entity ──────────────────────────────────────────
    private void fromDTO(VehicleDTO dto, Vehicle e) {
        e.setVehicleCode(dto.getVehicleCode());
        e.setVehicleName(dto.getVehicleName());
        e.setVehicleNumber(dto.getVehicleNumber());

        // Category
        if (dto.getCategoryCode() != null) {
            categoryRepository.findById(dto.getCategoryCode())
                    .ifPresent(e::setCategory);
        }

        e.setBrand(dto.getBrand());
        e.setModel(dto.getModel());
        e.setVehicleYear(dto.getVehicleYear());
        e.setColor(dto.getColor());
        e.setFuelType(dto.getFuelType());
        e.setEngineCc(dto.getEngineCc());
        e.setChassisNumber(dto.getChassisNumber());

        // Capacity
        e.setCapacityWeight(dto.getCapacityWeight());
        e.setCapacityVolume(dto.getCapacityVolume());
        e.setVolumeUnit(dto.getVolumeUnit());
        e.setWeightUnit(dto.getWeightUnit());

        // Site / Depot
        e.setSite(dto.getSite());
        e.setDepartureSite(dto.getDepartureSite());
        e.setArrivalSite(dto.getArrivalSite());
        e.setArrivalDeparture(dto.getArrivalDeparture());

        // Timing
        e.setEarliestStartTime(dto.getEarliestStartTime());
        e.setMaxTotalTime(dto.getMaxTotalTime());
        e.setMaxTotalTravel(dto.getMaxTotalTravel());
        e.setMaxTotalDistance(dto.getMaxTotalDistance());
        e.setMaxOrderCount(dto.getMaxOrderCount());

        // Cost
        e.setFixedCost(dto.getFixedCost());
        e.setCostPerTime(dto.getCostPerTime());
        e.setCostPerDistance(dto.getCostPerDistance());
        e.setOvertimeStart(dto.getOvertimeStart());
        e.setOvertimeCost(dto.getOvertimeCost());

        // Dimensions
        e.setVehicleLength(dto.getVehicleLength());
        e.setVehicleWidth(dto.getVehicleWidth());
        e.setVehicleHeight(dto.getVehicleHeight());
        e.setEmptyMass(dto.getEmptyMass());
        e.setGrossMass(dto.getGrossMass());
        e.setTolerance(dto.getTolerance());

        // Driver / Trailer
        e.setDriverId(dto.getDriverId());
        e.setTrailerNumber(dto.getTrailerNumber());
        e.setAllowAllDrivers(dto.getAllowAllDrivers());

        // Tracking
        e.setGpsId(dto.getGpsId());
        e.setMobileTracker(dto.getMobileTracker());
        e.setOdometer(dto.getOdometer());
        e.setCurrentMeter(dto.getCurrentMeter());

        // Licensing
        e.setLicenseReference(dto.getLicenseReference());
        e.setLicenseExpiry(dto.getLicenseExpiry());
        e.setInsuranceReference(dto.getInsuranceReference());
        e.setInsuranceExpiry(dto.getInsuranceExpiry());
        e.setLastInspectionDate(dto.getLastInspectionDate());
        e.setInspectionExpiry(dto.getInspectionExpiry());

        // Other
        e.setSpecialtyName(dto.getSpecialtyName());
        e.setAssignmentRule(dto.getAssignmentRule());
        e.setEquipmentNotes(dto.getEquipmentNotes());
        e.setExternalVehicle(dto.getExternalVehicle());

        // Image — decode Base64 → binary
        if (dto.getImage() != null && !dto.getImage().isBlank()) {
            try {
                String b64 = dto.getImage().contains(",")
                        ? dto.getImage().split(",")[1]
                        : dto.getImage();
                e.setVehicleImage(Base64.getDecoder().decode(b64));
            } catch (Exception ignored) {}
        }

        // Status
        e.setActive(dto.getActive() != null ? dto.getActive() : true);
        e.setVehicleStatus(dto.getVehicleStatus());
    }

    // ── Entity → DTO ──────────────────────────────────────────
    private VehicleDTO mapToDTO(Vehicle e) {
        VehicleDTO dto = new VehicleDTO();
        dto.setVehicleCode(e.getVehicleCode());
        dto.setVehicleName(e.getVehicleName());
        dto.setVehicleNumber(e.getVehicleNumber());

        if (e.getCategory() != null) {
            dto.setCategoryCode(e.getCategory().getCategoryCode());
            dto.setCategoryDescription(e.getCategory().getDescription());
        }

        dto.setBrand(e.getBrand());
        dto.setModel(e.getModel());
        dto.setVehicleYear(e.getVehicleYear());
        dto.setColor(e.getColor());
        dto.setFuelType(e.getFuelType());
        dto.setEngineCc(e.getEngineCc());
        dto.setChassisNumber(e.getChassisNumber());

        dto.setCapacityWeight(e.getCapacityWeight());
        dto.setCapacityVolume(e.getCapacityVolume());
        dto.setVolumeUnit(e.getVolumeUnit());
        dto.setWeightUnit(e.getWeightUnit());

        dto.setSite(e.getSite());
        dto.setDepartureSite(e.getDepartureSite());
        dto.setArrivalSite(e.getArrivalSite());
        dto.setArrivalDeparture(e.getArrivalDeparture());

        dto.setEarliestStartTime(e.getEarliestStartTime());
        dto.setMaxTotalTime(e.getMaxTotalTime());
        dto.setMaxTotalTravel(e.getMaxTotalTravel());
        dto.setMaxTotalDistance(e.getMaxTotalDistance());
        dto.setMaxOrderCount(e.getMaxOrderCount());

        dto.setFixedCost(e.getFixedCost());
        dto.setCostPerTime(e.getCostPerTime());
        dto.setCostPerDistance(e.getCostPerDistance());
        dto.setOvertimeStart(e.getOvertimeStart());
        dto.setOvertimeCost(e.getOvertimeCost());

        dto.setVehicleLength(e.getVehicleLength());
        dto.setVehicleWidth(e.getVehicleWidth());
        dto.setVehicleHeight(e.getVehicleHeight());
        dto.setEmptyMass(e.getEmptyMass());
        dto.setGrossMass(e.getGrossMass());
        dto.setTolerance(e.getTolerance());

        dto.setDriverId(e.getDriverId());
        dto.setTrailerNumber(e.getTrailerNumber());
        dto.setAllowAllDrivers(e.getAllowAllDrivers());

        dto.setGpsId(e.getGpsId());
        dto.setMobileTracker(e.getMobileTracker());
        dto.setOdometer(e.getOdometer());
        dto.setCurrentMeter(e.getCurrentMeter());

        dto.setLicenseReference(e.getLicenseReference());
        dto.setLicenseExpiry(e.getLicenseExpiry());
        dto.setInsuranceReference(e.getInsuranceReference());
        dto.setInsuranceExpiry(e.getInsuranceExpiry());
        dto.setLastInspectionDate(e.getLastInspectionDate());
        dto.setInspectionExpiry(e.getInspectionExpiry());

        dto.setSpecialtyName(e.getSpecialtyName());
        dto.setAssignmentRule(e.getAssignmentRule());
        dto.setEquipmentNotes(e.getEquipmentNotes());
        dto.setExternalVehicle(e.getExternalVehicle());

        // Image — encode binary → Base64
        if (e.getVehicleImage() != null && e.getVehicleImage().length > 0) {
            dto.setImage("data:image/jpeg;base64,"
                    + Base64.getEncoder().encodeToString(e.getVehicleImage()));
        }

        dto.setActive(e.getActive());
        dto.setVehicleStatus(e.getVehicleStatus());
        dto.setCreatedBy(e.getCreatedBy());
        dto.setCreatedAt(e.getCreatedAt());
        dto.setUpdatedBy(e.getUpdatedBy());
        dto.setUpdatedAt(e.getUpdatedAt());

        return dto;
    }
}
