package com.transport.tms.Fleet.Impl;

import com.transport.tms.Fleet.Dto.VehicleDriverAssignmentDTO;
import com.transport.tms.Fleet.Entity.Driver;
import com.transport.tms.Fleet.Entity.Vehicle;
import com.transport.tms.Fleet.Entity.VehicleDriverAssignment;
import com.transport.tms.Fleet.Repository.DriverRepository;
import com.transport.tms.Fleet.Repository.VehicleDriverAssignmentRepository;
import com.transport.tms.Fleet.Repository.VehicleRepository;
import com.transport.tms.Fleet.Service.VehicleDriverAssignmentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;




@Service
@RequiredArgsConstructor
@Transactional
public class VehicleDriverAssignmentServiceImpl
        implements VehicleDriverAssignmentService {

    private final VehicleDriverAssignmentRepository repository;

    private final VehicleRepository vehicleRepository;

    private final DriverRepository driverRepository;

    @Override
    public VehicleDriverAssignmentDTO create(
            VehicleDriverAssignmentDTO dto) {

        validateAssignment(dto);

        Vehicle vehicle =
                vehicleRepository.findById(
                                dto.getVehicleCode())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Vehicle not found"));

        Driver driver =
                driverRepository.findById(
                                dto.getDriverId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Driver not found"));

        VehicleDriverAssignment entity =
                new VehicleDriverAssignment();

        entity.setVehicle(vehicle);
        entity.setDriver(driver);

        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());

        entity.setActive(dto.getActive());
        entity.setRemarks(dto.getRemarks());

        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        return mapToDTO(
                repository.save(entity));
    }

    @Override
    public VehicleDriverAssignmentDTO update(
            UUID assignmentId,
            VehicleDriverAssignmentDTO dto) {

        VehicleDriverAssignment entity =
                repository.findById(assignmentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Assignment not found"));

        validateAssignment(dto);

        Vehicle vehicle =
                vehicleRepository.findById(
                                dto.getVehicleCode())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Vehicle not found"));

        Driver driver =
                driverRepository.findById(
                                dto.getDriverId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Driver not found"));

        entity.setVehicle(vehicle);
        entity.setDriver(driver);

        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());

        entity.setActive(dto.getActive());
        entity.setRemarks(dto.getRemarks());

        entity.setUpdatedAt(LocalDateTime.now());

        return mapToDTO(
                repository.save(entity));
    }

    @Override
    public VehicleDriverAssignmentDTO getById(
            UUID assignmentId) {

        return mapToDTO(
                repository.findById(assignmentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Assignment not found")));
    }

    @Override
    public List<VehicleDriverAssignmentDTO> getAll() {

        return repository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public void delete(
            UUID assignmentId) {

        repository.deleteById(
                assignmentId);
    }

    private void validateAssignment(
            VehicleDriverAssignmentDTO dto) {

        LocalDate endDate =
                dto.getEndDate() == null
                        ? LocalDate.of(2999, 12, 31)
                        : dto.getEndDate();

        if(repository.countDriverOverlap(
                dto.getDriverId(),
                dto.getStartDate(),
                endDate) > 0) {

            throw new RuntimeException(
                    "Driver already assigned during selected period");
        }

        if(repository.countVehicleOverlap(
                dto.getVehicleCode(),
                dto.getStartDate(),
                endDate) > 0) {

            throw new RuntimeException(
                    "Vehicle already assigned during selected period");
        }
    }

    private VehicleDriverAssignmentDTO mapToDTO(
            VehicleDriverAssignment entity) {

        VehicleDriverAssignmentDTO dto =
                new VehicleDriverAssignmentDTO();

        dto.setAssignmentId(
                entity.getAssignmentId());

        dto.setVehicleCode(
                entity.getVehicle().getVehicleCode());

        dto.setVehicleName(
                entity.getVehicle().getVehicleName());

        dto.setDriverId(
                entity.getDriver().getDriverId());

        dto.setDriverName(
                entity.getDriver().getDriverName());

        dto.setStartDate(
                entity.getStartDate());

        dto.setEndDate(
                entity.getEndDate());

        dto.setActive(
                entity.getActive());

        dto.setRemarks(
                entity.getRemarks());

        return dto;
    }
}