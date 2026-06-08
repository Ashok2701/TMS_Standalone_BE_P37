package com.transport.tms.Sync.Customer.Service;

import com.transport.tms.Sync.Customer.Dto.*;
import com.transport.tms.Sync.Customer.Entity.*;
import com.transport.tms.Sync.Customer.Repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressTmsService {

    private final CustomerAddressRepository addressRepository;

    private final AddressTimeWindowRepository timeWindowRepository;

    private final AddressVehicleRepository vehicleRepository;

    private final AddressDriverRepository driverRepository;

    // ── GET full TMS data for an address ───────────────────────
    public AddressTmsDTO getTmsData(String addressCode) {

        XRCustomerAddress address = addressRepository.findById(addressCode)
                .orElseThrow(() ->
                        new RuntimeException("Address not found: " + addressCode));

        AddressTmsDTO dto = new AddressTmsDTO();

        dto.setAnyTimeWindow(address.getAnyTimeWindow());
        dto.setAnyVehicleCategory(address.getAnyVehicleCategory());
        dto.setAnyDriver(address.getAnyDriver());

        // Time windows
        dto.setTimeWindows(
                timeWindowRepository.findByAddressAddressCode(addressCode)
                        .stream()
                        .map(this::mapTimeWindow)
                        .toList()
        );

        // Vehicles
        dto.setVehicles(
                vehicleRepository.findByAddressAddressCode(addressCode)
                        .stream()
                        .map(this::mapVehicle)
                        .toList()
        );

        // Drivers
        dto.setDrivers(
                driverRepository.findByAddressAddressCode(addressCode)
                        .stream()
                        .map(this::mapDriver)
                        .toList()
        );

        return dto;
    }

    // ── SAVE full TMS data for an address ──────────────────────
    // Replaces all grids cleanly on each save
    @Transactional
    public AddressTmsDTO saveTmsData(String addressCode,
                                     AddressTmsDTO dto) {

        XRCustomerAddress address = addressRepository.findById(addressCode)
                .orElseThrow(() ->
                        new RuntimeException("Address not found: " + addressCode));

        // Update flags
        address.setAnyTimeWindow(dto.getAnyTimeWindow());
        address.setAnyVehicleCategory(dto.getAnyVehicleCategory());
        address.setAnyDriver(dto.getAnyDriver());
        address.setUpdatedBy(dto.getUpdatedBy());
        address.setUpdatedAt(LocalDateTime.now());
        addressRepository.save(address);

        // ── Time windows — delete all then re-insert ────────────
        timeWindowRepository.deleteByAddressAddressCode(addressCode);
        if (dto.getTimeWindows() != null) {
            int order = 0;
            for (TimeWindowDTO tw : dto.getTimeWindows()) {
                XRAddressTimeWindow entity = new XRAddressTimeWindow();
                entity.setAddress(address);
                entity.setFromTime(tw.getFromTime());
                entity.setToTime(tw.getToTime());
                entity.setDisplayOrder(order++);
                entity.setCreatedAt(LocalDateTime.now());
                entity.setUpdatedAt(LocalDateTime.now());
                timeWindowRepository.save(entity);
            }
        }

        // ── Vehicle categories — delete all then re-insert ──────
        vehicleRepository.deleteByAddressAddressCode(addressCode);
        if (dto.getVehicles() != null) {
            for (AddressVehicleDTO v : dto.getVehicles()) {
                XRAddressVehicle entity = new XRAddressVehicle();
                entity.setAddress(address);
                entity.setVehicleCategoryCode(v.getVehicleCategoryCode());
                entity.setCreatedAt(LocalDateTime.now());
                entity.setUpdatedAt(LocalDateTime.now());
                vehicleRepository.save(entity);
            }
        }

        // ── Drivers — delete all then re-insert ─────────────────
        driverRepository.deleteByAddressAddressCode(addressCode);
        if (dto.getDrivers() != null) {
            for (AddressDriverDTO d : dto.getDrivers()) {
                XRAddressDriver entity = new XRAddressDriver();
                entity.setAddress(address);
                entity.setDriverId(d.getDriverId());
                entity.setCreatedAt(LocalDateTime.now());
                entity.setUpdatedAt(LocalDateTime.now());
                driverRepository.save(entity);
            }
        }

        return getTmsData(addressCode);
    }

    // ── Mappers ────────────────────────────────────────────────
    private TimeWindowDTO mapTimeWindow(XRAddressTimeWindow e) {
        TimeWindowDTO dto = new TimeWindowDTO();
        dto.setId(e.getId());
        dto.setFromTime(e.getFromTime());
        dto.setToTime(e.getToTime());
        dto.setDisplayOrder(e.getDisplayOrder());
        return dto;
    }

    private AddressVehicleDTO mapVehicle(XRAddressVehicle e) {
        AddressVehicleDTO dto = new AddressVehicleDTO();
        dto.setId(e.getId());
        dto.setVehicleCategoryCode(e.getVehicleCategoryCode());
        return dto;
    }

    private AddressDriverDTO mapDriver(XRAddressDriver e) {
        AddressDriverDTO dto = new AddressDriverDTO();
        dto.setId(e.getId());
        dto.setDriverId(e.getDriverId());
        return dto;
    }
}
