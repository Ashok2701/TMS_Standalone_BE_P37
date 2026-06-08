package com.transport.tms.Fleet.Impl;

import com.transport.tms.Fleet.Dto.VehicleCategoryDTO;
import com.transport.tms.Fleet.Entity.VehicleCategory;
import com.transport.tms.Fleet.Repository.VehicleCategoryRepository;
import com.transport.tms.Fleet.Service.VehicleCategoryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class VehicleCategoryServiceImpl
        implements VehicleCategoryService {

    private final VehicleCategoryRepository repository;

    @Override
    public VehicleCategoryDTO create(
            VehicleCategoryDTO dto) {

        if(repository.existsByCategoryCode(
                dto.getCategoryCode())) {

            throw new RuntimeException(
                    "Vehicle Category already exists");
        }

        VehicleCategory entity =
                mapToEntity(dto);

        entity.setCreatedAt(
                LocalDateTime.now());

        entity.setUpdatedAt(
                LocalDateTime.now());

        return mapToDTO(
                repository.save(entity));
    }

    @Override
    public VehicleCategoryDTO update(
            String categoryCode,
            VehicleCategoryDTO dto) {

        VehicleCategory entity =
                repository.findById(categoryCode)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Vehicle Category not found"));

        entity.setDescription(dto.getDescription());
        entity.setActive(dto.getActive());
        entity.setCountryCode(dto.getCountryCode());
        entity.setVehicleType(dto.getVehicleType());
        entity.setAxleCount(dto.getAxleCount());
        entity.setMaxCapacityWt(dto.getMaxCapacityWt());
        entity.setMaxCapacityVol(dto.getMaxCapacityVol());
        entity.setVolumeUnit(dto.getVolumeUnit());
        entity.setWeightUnit(dto.getWeightUnit());
        entity.setSkillNumber(dto.getSkillNumber());
        entity.setInspectionIn(dto.getInspectionIn());
        entity.setManualIn(dto.getManualIn());
        entity.setInspectionOut(dto.getInspectionOut());
        entity.setManualOut(dto.getManualOut());

        entity.setUpdatedAt(
                LocalDateTime.now());

        return mapToDTO(
                repository.save(entity));
    }

    @Override
    public VehicleCategoryDTO getById(
            String categoryCode) {

        return mapToDTO(
                repository.findById(categoryCode)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Vehicle Category not found")));
    }

    @Override
    public List<VehicleCategoryDTO> getAll() {

        return repository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public void delete(
            String categoryCode) {

        repository.deleteById(categoryCode);
    }

    private VehicleCategoryDTO mapToDTO(
            VehicleCategory entity) {

        VehicleCategoryDTO dto =
                new VehicleCategoryDTO();

        dto.setCategoryCode(entity.getCategoryCode());
        dto.setDescription(entity.getDescription());
        dto.setActive(entity.getActive());
        dto.setCountryCode(entity.getCountryCode());
        dto.setVehicleType(entity.getVehicleType());
        dto.setAxleCount(entity.getAxleCount());
        dto.setMaxCapacityWt(entity.getMaxCapacityWt());
        dto.setMaxCapacityVol(entity.getMaxCapacityVol());
        dto.setVolumeUnit(entity.getVolumeUnit());
        dto.setWeightUnit(entity.getWeightUnit());
        dto.setSkillNumber(entity.getSkillNumber());
        dto.setInspectionIn(entity.getInspectionIn());
        dto.setManualIn(entity.getManualIn());
        dto.setInspectionOut(entity.getInspectionOut());
        dto.setManualOut(entity.getManualOut());

        return dto;
    }

    private VehicleCategory mapToEntity(
            VehicleCategoryDTO dto) {

        VehicleCategory entity =
                new VehicleCategory();

        entity.setCategoryCode(dto.getCategoryCode());
        entity.setDescription(dto.getDescription());
        entity.setActive(dto.getActive());

        entity.setCountryCode(dto.getCountryCode());
        entity.setVehicleType(dto.getVehicleType());
        entity.setAxleCount(dto.getAxleCount());

        entity.setMaxCapacityWt(dto.getMaxCapacityWt());
        entity.setMaxCapacityVol(dto.getMaxCapacityVol());

        entity.setVolumeUnit(dto.getVolumeUnit());
        entity.setWeightUnit(dto.getWeightUnit());

        entity.setSkillNumber(dto.getSkillNumber());

        entity.setInspectionIn(dto.getInspectionIn());
        entity.setManualIn(dto.getManualIn());

        entity.setInspectionOut(dto.getInspectionOut());
        entity.setManualOut(dto.getManualOut());

        return entity;
    }
}