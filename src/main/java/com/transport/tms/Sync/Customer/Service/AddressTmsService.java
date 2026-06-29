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

    // ── GET TMS data ──────────────────────────────────────────
    public AddressTmsDTO getTmsData(String customerCode, String addressCode) {

        XRCustomerAddress address =
                addressRepository.findByCustomerCodeAndAddressCode(
                                customerCode, addressCode)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Address not found: " + customerCode + "/" + addressCode));

        AddressTmsDTO dto = new AddressTmsDTO();
        dto.setLatitude(address.getLatitude());
        dto.setLongitude(address.getLongitude());
        dto.setAnyTimeWindow(address.getAnyTimeWindow());
        dto.setAnyVehicleCategory(address.getAnyVehicleCategory());
        dto.setAnyDriver(address.getAnyDriver());

        dto.setTimeWindows(
                timeWindowRepository
                        .findByAddressCustomerCodeAndAddressAddressCode(customerCode, addressCode)
                        .stream().map(this::mapTimeWindow).toList());

        dto.setVehicles(
                vehicleRepository
                        .findByAddressCustomerCodeAndAddressAddressCode(customerCode, addressCode)
                        .stream().map(this::mapVehicle).toList());

        dto.setDrivers(
                driverRepository
                        .findByAddressCustomerCodeAndAddressAddressCode(customerCode, addressCode)
                        .stream().map(this::mapDriver).toList());

        return dto;
    }

    // ── SAVE TMS data ─────────────────────────────────────────
    @Transactional
    public AddressTmsDTO saveTmsData(String customerCode,
                                     String addressCode,
                                     AddressTmsDTO dto) {

        XRCustomerAddress address =
                addressRepository.findByCustomerCodeAndAddressCode(
                                customerCode, addressCode)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Address not found: " + customerCode + "/" + addressCode));

        if (dto.getLatitude()    != null) address.setLatitude(dto.getLatitude());
        if (dto.getLongitude()   != null) address.setLongitude(dto.getLongitude());
        address.setAnyTimeWindow(dto.getAnyTimeWindow());
        address.setAnyVehicleCategory(dto.getAnyVehicleCategory());
        address.setAnyDriver(dto.getAnyDriver());
        address.setUpdatedBy(dto.getUpdatedBy());
        address.setUpdatedAt(LocalDateTime.now());
        addressRepository.save(address);

        // Time windows — replace all
        timeWindowRepository.deleteByAddressCustomerCodeAndAddressAddressCode(
                customerCode, addressCode);
        if (dto.getTimeWindows() != null) {
            int order = 0;
            for (TimeWindowDTO tw : dto.getTimeWindows()) {
                XRAddressTimeWindow e = new XRAddressTimeWindow();
                e.setAddress(address);
                e.setFromTime(tw.getFromTime());
                e.setToTime(tw.getToTime());
                e.setDisplayOrder(order++);
                e.setCreatedAt(LocalDateTime.now());
                e.setUpdatedAt(LocalDateTime.now());
                timeWindowRepository.save(e);
            }
        }

        // Vehicle categories — replace all
        vehicleRepository.deleteByAddressCustomerCodeAndAddressAddressCode(
                customerCode, addressCode);
        if (dto.getVehicles() != null) {
            for (AddressVehicleDTO v : dto.getVehicles()) {
                XRAddressVehicle e = new XRAddressVehicle();
                e.setAddress(address);
                e.setVehicleCategoryCode(v.getVehicleCategoryCode());
                e.setCreatedAt(LocalDateTime.now());
                e.setUpdatedAt(LocalDateTime.now());
                vehicleRepository.save(e);
            }
        }

        // Drivers — replace all
        driverRepository.deleteByAddressCustomerCodeAndAddressAddressCode(
                customerCode, addressCode);
        if (dto.getDrivers() != null) {
            for (AddressDriverDTO d : dto.getDrivers()) {
                XRAddressDriver e = new XRAddressDriver();
                e.setAddress(address);
                e.setDriverId(d.getDriverId());
                e.setCreatedAt(LocalDateTime.now());
                e.setUpdatedAt(LocalDateTime.now());
                driverRepository.save(e);
            }
        }

        return getTmsData(customerCode, addressCode);
    }

    // ── Mappers ───────────────────────────────────────────────
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
