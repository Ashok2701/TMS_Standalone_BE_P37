package com.transport.tms.Fleet.Impl;


import com.transport.tms.Fleet.Dto.DriverDTO;
import com.transport.tms.Fleet.Entity.Driver;
import com.transport.tms.Fleet.Repository.DriverRepository;
import com.transport.tms.Fleet.Service.DriverService;
import lombok.RequiredArgsConstructor;



import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DriverServiceImpl
        implements DriverService {

    private final DriverRepository repository;

    @Override
    public DriverDTO create(
            DriverDTO dto) {

        if(repository.existsByDriverId(
                dto.getDriverId())) {

            throw new RuntimeException(
                    "Driver already exists");
        }

        Driver entity =
                mapToEntity(dto);

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
    public DriverDTO update(
            String driverId,
            DriverDTO dto) {

        Driver entity =
                repository.findById(driverId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Driver not found"));

        entity.setDriverName(dto.getDriverName());
        entity.setActive(dto.getActive());
        entity.setEmployeeCode(dto.getEmployeeCode());
        entity.setDateOfBirth(dto.getDateOfBirth());
        entity.setMobileNo(dto.getMobileNo());
        entity.setAlternateMobile(dto.getAlternateMobile());
        entity.setEmail(dto.getEmail());
        entity.setAddressLine1(dto.getAddressLine1());
        entity.setAddressLine2(dto.getAddressLine2());
        entity.setCity(dto.getCity());
        entity.setPostalCode(dto.getPostalCode());
        entity.setCountryCode(dto.getCountryCode());
        entity.setLicenseNumber(dto.getLicenseNumber());
        entity.setLicenseType(dto.getLicenseType());
        entity.setLicenseIssueDate(dto.getLicenseIssueDate());
        entity.setLicenseExpiryDate(dto.getLicenseExpiryDate());
        entity.setIssuedBy(dto.getIssuedBy());
        entity.setLastMedicalDate(dto.getLastMedicalDate());
        entity.setMaxHoursPerDay(dto.getMaxHoursPerDay());
        entity.setMaxHoursPerWeek(dto.getMaxHoursPerWeek());
        entity.setDriverStatus(dto.getDriverStatus());
        entity.setAllowAllVehicles(dto.getAllowAllVehicles());
        entity.setLongHaulDriver(dto.getLongHaulDriver());
        entity.setNotes(dto.getNotes());
        if (dto.getImage() != null && !dto.getImage().isBlank()) {
            try {
                String b64 = dto.getImage().contains(",")
                        ? dto.getImage().split(",")[1]
                        : dto.getImage();
                entity.setDriverImage(Base64.getDecoder().decode(b64));
            } catch (Exception ignored) {}
        }
        entity.setUpdatedAt(
                LocalDateTime.now());

        return mapToDTO(
                repository.save(entity));
    }

    @Override
    public DriverDTO getById(
            String driverId) {

        return mapToDTO(
                repository.findById(driverId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Driver not found")));
    }

    @Override
    public List<DriverDTO> getAll() {

        return repository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public void delete(
            String driverId) {

        repository.deleteById(driverId);
    }

    private DriverDTO mapToDTO(Driver entity) {
        DriverDTO dto = new DriverDTO();
        BeanUtils.copyProperties(entity, dto);
        // Image: encode binary → Base64
        if (entity.getDriverImage() != null && entity.getDriverImage().length > 0) {
            dto.setImage("data:image/jpeg;base64,"
                    + Base64.getEncoder().encodeToString(entity.getDriverImage()));
        }
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    private Driver mapToEntity(DriverDTO dto) {
        Driver entity = new Driver();
        BeanUtils.copyProperties(dto, entity);
        // Image: decode Base64 → binary
        if (dto.getImage() != null && !dto.getImage().isBlank()) {
            try {
                String b64 = dto.getImage().contains(",")
                        ? dto.getImage().split(",")[1]
                        : dto.getImage();
                entity.setDriverImage(Base64.getDecoder().decode(b64));
            } catch (Exception ignored) {}
        }
        return entity;
    }
}