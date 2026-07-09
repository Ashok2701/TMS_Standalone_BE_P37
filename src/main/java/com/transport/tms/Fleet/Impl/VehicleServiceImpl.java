package com.transport.tms.Fleet.Impl;

import com.transport.tms.Fleet.Dto.VehicleDTO;
import com.transport.tms.Fleet.Entity.Vehicle;
import com.transport.tms.Fleet.Entity.VehicleCategory;
import com.transport.tms.Fleet.Repository.VehicleCategoryRepository;
import com.transport.tms.Fleet.Repository.VehicleRepository;
import com.transport.tms.Fleet.Service.VehicleService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class VehicleServiceImpl
        implements VehicleService {

    private final VehicleRepository repository;

    private final VehicleCategoryRepository categoryRepository;

    @Override
    public VehicleDTO create(
            VehicleDTO dto) {

        if(repository.existsByVehicleCode(
                dto.getVehicleCode())) {

            throw new RuntimeException(
                    "Vehicle Code already exists");
        }

        VehicleCategory category =
                categoryRepository.findById(
                                dto.getCategoryCode())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Vehicle Category not found"));

        Vehicle entity =
                new Vehicle();

        entity.setVehicleCode(
                dto.getVehicleCode());

        entity.setVehicleName(
                dto.getVehicleName());

        entity.setVehicleNumber(
                dto.getVehicleNumber());

        entity.setCategory(
                category);

        entity.setBrand(
                dto.getBrand());

        entity.setModel(
                dto.getModel());

        entity.setVehicleYear(
                dto.getVehicleYear());

        entity.setColor(
                dto.getColor());

        entity.setCapacityWeight(
                dto.getCapacityWeight());

        entity.setCapacityVolume(
                dto.getCapacityVolume());

        entity.setVolumeUnit(
                dto.getVolumeUnit());

        entity.setWeightUnit(
                dto.getWeightUnit());

        entity.setDriverId(
                dto.getDriverId());

        entity.setSite(dto.getSite());
        entity.setDepartureSite(dto.getDepartureSite());
        entity.setArrivalSite(dto.getArrivalSite());
        entity.setStartTime(dto.getStartTime());
        entity.setMaxPallets(dto.getMaxPallets());
        entity.setMaxCases(dto.getMaxCases());

        entity.setVehicleStatus(
                dto.getVehicleStatus());

        entity.setActive(
                dto.getActive());

        entity.setCreatedAt(
                LocalDateTime.now());

        entity.setUpdatedAt(
                LocalDateTime.now());

        entity.setUuid(
                UUID.randomUUID());

        return mapToDTO(
                repository.save(entity));
    }

    @Override
    public VehicleDTO update(
            String vehicleCode,
            VehicleDTO dto) {

        Vehicle entity =
                repository.findById(vehicleCode)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Vehicle not found"));

        VehicleCategory category =
                categoryRepository.findById(
                                dto.getCategoryCode())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Vehicle Category not found"));

        entity.setVehicleName(dto.getVehicleName());
        entity.setVehicleNumber(dto.getVehicleNumber());
        entity.setCategory(category);
        entity.setBrand(dto.getBrand());
        entity.setModel(dto.getModel());
        entity.setVehicleYear(dto.getVehicleYear());
        entity.setColor(dto.getColor());
        entity.setCapacityWeight(dto.getCapacityWeight());
        entity.setCapacityVolume(dto.getCapacityVolume());
        entity.setVolumeUnit(dto.getVolumeUnit());
        entity.setWeightUnit(dto.getWeightUnit());
        entity.setDriverId(dto.getDriverId());
        entity.setSite(dto.getSite());
        if (dto.getImage() != null && !dto.getImage().isBlank()) {
            try {
                String b64 = dto.getImage().contains(",")
                        ? dto.getImage().split(",")[1]
                        : dto.getImage();
                entity.setImage(Base64.getDecoder().decode(b64));
            } catch (Exception ignored) {}
        }
        entity.setDepartureSite(dto.getDepartureSite());
        entity.setArrivalSite(dto.getArrivalSite());
        entity.setStartTime(dto.getStartTime());
        entity.setMaxPallets(dto.getMaxPallets());
        entity.setMaxCases(dto.getMaxCases());
        entity.setVehicleStatus(dto.getVehicleStatus());
        entity.setActive(dto.getActive());

        entity.setUpdatedAt(
                LocalDateTime.now());

        return mapToDTO(
                repository.save(entity));
    }

    @Override
    public VehicleDTO getById(
            String vehicleCode) {

        return mapToDTO(
                repository.findById(vehicleCode)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Vehicle not found")));
    }

    @Override
    public List<VehicleDTO> getAll() {

        return repository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public void delete(
            String vehicleCode) {

        repository.deleteById(vehicleCode);
    }

    private VehicleDTO mapToDTO(
            Vehicle entity) {

        VehicleDTO dto =
                new VehicleDTO();

        dto.setVehicleCode(entity.getVehicleCode());
        dto.setVehicleName(entity.getVehicleName());
        dto.setVehicleNumber(entity.getVehicleNumber());

        dto.setCategoryCode(
                entity.getCategory().getCategoryCode());

        dto.setCategoryDescription(
                entity.getCategory().getDescription());

        dto.setBrand(entity.getBrand());
        dto.setModel(entity.getModel());
        dto.setVehicleYear(entity.getVehicleYear());
        dto.setColor(entity.getColor());

        dto.setCapacityWeight(entity.getCapacityWeight());
        dto.setCapacityVolume(entity.getCapacityVolume());

        dto.setVolumeUnit(entity.getVolumeUnit());
        dto.setWeightUnit(entity.getWeightUnit());

        dto.setDriverId(entity.getDriverId());
        dto.setSite(entity.getSite());
        // Image: encode binary → Base64 string for JSON
        if (entity.getImage() != null && entity.getImage().length > 0) {
            dto.setImage("data:image/jpeg;base64,"
                    + Base64.getEncoder().encodeToString(entity.getImage()));
        }
        dto.setDepartureSite(entity.getDepartureSite());
        dto.setArrivalSite(entity.getArrivalSite());
        dto.setStartTime(entity.getStartTime());
        dto.setMaxPallets(entity.getMaxPallets());
        dto.setMaxCases(entity.getMaxCases());

        dto.setVehicleStatus(entity.getVehicleStatus());

        dto.setActive(entity.getActive());

        return dto;
    }
}