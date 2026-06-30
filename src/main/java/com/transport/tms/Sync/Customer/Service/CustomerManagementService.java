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
public class CustomerManagementService {

    private final CustomerRepository customerRepository;
    private final CustomerAddressRepository addressRepository;
    private final AddressTimeWindowRepository timeWindowRepository;
    private final AddressVehicleRepository vehicleRepository;
    private final AddressDriverRepository driverRepository;

    // ── LIST ──────────────────────────────────────────────────
    public List<CustomerListDTO> listCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(c -> {
                    CustomerListDTO dto = new CustomerListDTO();
                    dto.setCustomerCode(c.getCustomerCode());
                    dto.setCustomerName(c.getCustomerName());
                    dto.setShortName(c.getShortName());
                    dto.setCountryCode(c.getCountryCode());
                    dto.setCurrencyCode(c.getCurrencyCode());
                    dto.setActive(c.getActive());
                    dto.setLatitude(c.getLatitude());
                    dto.setLongitude(c.getLongitude());
                    dto.setServiceTime(c.getServiceTime());
                    dto.setWaitingTime(c.getWaitingTime());
                    dto.setAddressCount(
                            addressRepository.countByCustomerCode(c.getCustomerCode()));
                    return dto;
                })
                .toList();
    }

    // ── GET DETAIL ────────────────────────────────────────────
    public CustomerDetailDTO getCustomerDetail(String customerCode) {

        XRCustomer customer = customerRepository.findById(customerCode)
                .orElseThrow(() ->
                        new RuntimeException("Customer not found: " + customerCode));

        List<CustomerAddressDetailDTO> addresses =
                addressRepository.findByCustomerCode(customerCode)
                        .stream()
                        .map(a -> mapAddressDetail(a))
                        .toList();

        return mapCustomerDetail(customer, addresses);
    }

    // ── UPDATE CUSTOMER TMS FIELDS ────────────────────────────
    @Transactional
    public CustomerDetailDTO updateCustomerTms(String customerCode,
                                               CustomerDetailDTO dto) {

        XRCustomer customer = customerRepository.findById(customerCode)
                .orElseThrow(() ->
                        new RuntimeException("Customer not found: " + customerCode));

        customer.setLatitude(dto.getLatitude());
        customer.setLongitude(dto.getLongitude());
        customer.setServiceTime(dto.getServiceTime());
        customer.setWaitingTime(dto.getWaitingTime());
        customer.setUpdatedBy(dto.getUpdatedBy());
        customer.setUpdatedAt(LocalDateTime.now());
        customerRepository.save(customer);

        return getCustomerDetail(customerCode);
    }

    // ── GET SINGLE ADDRESS ────────────────────────────────────
    public CustomerAddressDetailDTO getAddressDetail(String customerCode,
                                                     String addressCode) {

        XRCustomerAddress address =
                addressRepository.findByCustomerCodeAndAddressCode(
                                customerCode, addressCode)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Address not found: " + customerCode + "/" + addressCode));

        return mapAddressDetail(address);
    }

    // ── SAVE ADDRESS TMS + GRIDS ──────────────────────────────
    @Transactional
    public CustomerAddressDetailDTO saveAddressTms(String customerCode,
                                                   String addressCode,
                                                   CustomerAddressDetailDTO dto) {

        XRCustomerAddress address =
                addressRepository.findByCustomerCodeAndAddressCode(
                                customerCode, addressCode)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Address not found: " + customerCode + "/" + addressCode));

        // TMS geo — only overwrite if provided (avoid wiping on partial save)
        if (dto.getLatitude()  != null) address.setLatitude(dto.getLatitude());
        if (dto.getLongitude() != null) address.setLongitude(dto.getLongitude());

        // TMS flags
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

        return getAddressDetail(customerCode, addressCode);
    }

    // ── MAPPERS ───────────────────────────────────────────────
    private CustomerDetailDTO mapCustomerDetail(XRCustomer c,
                                                List<CustomerAddressDetailDTO> addresses) {
        CustomerDetailDTO dto = new CustomerDetailDTO();
        dto.setCustomerCode(c.getCustomerCode());
        dto.setCustomerName(c.getCustomerName());
        dto.setShortName(c.getShortName());
        dto.setCountryCode(c.getCountryCode());
        dto.setCurrencyCode(c.getCurrencyCode());
        dto.setActive(c.getActive());
        dto.setSyncedAt(c.getSyncedAt() != null ? c.getSyncedAt().toString() : null);
        dto.setLatitude(c.getLatitude());
        dto.setLongitude(c.getLongitude());
        dto.setServiceTime(c.getServiceTime());
        dto.setWaitingTime(c.getWaitingTime());
        dto.setUpdatedBy(c.getUpdatedBy());
        dto.setUpdatedAt(c.getUpdatedAt());
        dto.setAddresses(addresses);
        return dto;
    }

    private CustomerAddressDetailDTO mapAddressDetail(XRCustomerAddress a) {

        CustomerAddressDetailDTO dto = new CustomerAddressDetailDTO();
        dto.setAddressCode(a.getAddressCode());
        dto.setCustomerCode(a.getCustomerCode());
        dto.setAddressDescription(a.getAddressDescription());
        dto.setAddressLine1(a.getAddressLine1());
        dto.setAddressLine2(a.getAddressLine2());
        dto.setAddressLine3(a.getAddressLine3());
        dto.setCity(a.getCity());
        dto.setPostalCode(a.getPostalCode());
        dto.setStateCode(a.getStateCode());
        dto.setCountryCode(a.getCountryCode());
        dto.setCountryName(a.getCountryName());
        dto.setPhone(a.getPhone());
        dto.setMobile(a.getMobile());
        dto.setEmail(a.getEmail());
        dto.setWebSite(a.getWebSite());
        dto.setDefaultAddress(a.getDefaultAddress());
        dto.setSyncedAt(a.getSyncedAt());
        dto.setLatitude(a.getLatitude());
        dto.setLongitude(a.getLongitude());
        dto.setAnyTimeWindow(a.getAnyTimeWindow());
        dto.setAnyVehicleCategory(a.getAnyVehicleCategory());
        dto.setAnyDriver(a.getAnyDriver());
        dto.setUpdatedBy(a.getUpdatedBy());
        dto.setUpdatedAt(a.getUpdatedAt());

        String cCode = a.getCustomerCode();
        String aCode = a.getAddressCode();

        dto.setTimeWindows(
                timeWindowRepository
                        .findByAddressCustomerCodeAndAddressAddressCode(cCode, aCode)
                        .stream().map(tw -> {
                            TimeWindowDTO t = new TimeWindowDTO();
                            t.setId(tw.getId());
                            t.setFromTime(tw.getFromTime());
                            t.setToTime(tw.getToTime());
                            t.setDisplayOrder(tw.getDisplayOrder());
                            return t;
                        }).toList());

        dto.setVehicles(
                vehicleRepository
                        .findByAddressCustomerCodeAndAddressAddressCode(cCode, aCode)
                        .stream().map(v -> {
                            AddressVehicleDTO av = new AddressVehicleDTO();
                            av.setId(v.getId());
                            av.setVehicleCategoryCode(v.getVehicleCategoryCode());
                            return av;
                        }).toList());

        dto.setDrivers(
                driverRepository
                        .findByAddressCustomerCodeAndAddressAddressCode(cCode, aCode)
                        .stream().map(d -> {
                            AddressDriverDTO ad = new AddressDriverDTO();
                            ad.setId(d.getId());
                            ad.setDriverId(d.getDriverId());
                            return ad;
                        }).toList());

        return dto;
    }
}
