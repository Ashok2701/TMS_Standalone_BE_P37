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

    // ══════════════════════════════════════════════════════════
    // LIST — lightweight rows for table view
    // ══════════════════════════════════════════════════════════
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
                            addressRepository.countByCustomerCode(
                                    c.getCustomerCode()));
                    return dto;
                })
                .toList();
    }

    // ══════════════════════════════════════════════════════════
    // GET DETAIL — full customer + all addresses + all grids
    // ══════════════════════════════════════════════════════════
    public CustomerDetailDTO getCustomerDetail(String customerCode) {

        XRCustomer customer = customerRepository.findById(customerCode)
                .orElseThrow(() ->
                        new RuntimeException("Customer not found: " + customerCode));

        List<CustomerAddressDetailDTO> addresses =
                addressRepository.findByCustomerCode(customerCode)
                        .stream()
                        .map(this::mapAddressDetail)
                        .toList();

        return mapCustomerDetail(customer, addresses);
    }

    // ══════════════════════════════════════════════════════════
    // UPDATE CUSTOMER TMS FIELDS  (Info tab save)
    // ══════════════════════════════════════════════════════════
    @Transactional
    public CustomerDetailDTO updateCustomerTms(String customerCode,
                                               CustomerDetailDTO dto) {

        XRCustomer customer = customerRepository.findById(customerCode)
                .orElseThrow(() ->
                        new RuntimeException("Customer not found: " + customerCode));

        // Only TMS fields — never touch X3 fields
        customer.setLatitude(dto.getLatitude());
        customer.setLongitude(dto.getLongitude());
        customer.setServiceTime(dto.getServiceTime());
        customer.setWaitingTime(dto.getWaitingTime());
        customer.setUpdatedBy(dto.getUpdatedBy());
        customer.setUpdatedAt(LocalDateTime.now());

        customerRepository.save(customer);

        return getCustomerDetail(customerCode);
    }

    // ══════════════════════════════════════════════════════════
    // SAVE ADDRESS TMS FIELDS + GRIDS  (Address tab save)
    // ══════════════════════════════════════════════════════════
    @Transactional
    public CustomerAddressDetailDTO saveAddressTms(String addressCode,
                                                   CustomerAddressDetailDTO dto) {

        XRCustomerAddress address = addressRepository.findById(addressCode)
                .orElseThrow(() ->
                        new RuntimeException("Address not found: " + addressCode));

        // TMS flags
        address.setAnyTimeWindow(dto.getAnyTimeWindow());
        address.setAnyVehicleCategory(dto.getAnyVehicleCategory());
        address.setAnyDriver(dto.getAnyDriver());
        address.setUpdatedBy(dto.getUpdatedBy());
        address.setUpdatedAt(LocalDateTime.now());
        addressRepository.save(address);

        // ── Time windows — replace all ───────────────────────
        timeWindowRepository.deleteByAddressAddressCode(addressCode);
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

        // ── Vehicle categories — replace all ─────────────────
        vehicleRepository.deleteByAddressAddressCode(addressCode);
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

        // ── Drivers — replace all ─────────────────────────────
        driverRepository.deleteByAddressAddressCode(addressCode);
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

        return mapAddressDetail(
                addressRepository.findById(addressCode).get());
    }

    // ══════════════════════════════════════════════════════════
    // GET SINGLE ADDRESS DETAIL
    // ══════════════════════════════════════════════════════════
    public CustomerAddressDetailDTO getAddressDetail(String addressCode) {

        XRCustomerAddress address = addressRepository.findById(addressCode)
                .orElseThrow(() ->
                        new RuntimeException("Address not found: " + addressCode));

        return mapAddressDetail(address);
    }

    // ══════════════════════════════════════════════════════════
    // MAPPERS
    // ══════════════════════════════════════════════════════════
    private CustomerDetailDTO mapCustomerDetail(XRCustomer c,
                                                List<CustomerAddressDetailDTO> addresses) {
        CustomerDetailDTO dto = new CustomerDetailDTO();
        dto.setCustomerCode(c.getCustomerCode());
        dto.setCustomerName(c.getCustomerName());
        dto.setShortName(c.getShortName());
        dto.setCountryCode(c.getCountryCode());
        dto.setCurrencyCode(c.getCurrencyCode());
        dto.setActive(c.getActive());
        dto.setSyncedAt(c.getSyncedAt() != null
                ? c.getSyncedAt().toString() : null);
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
        dto.setAnyTimeWindow(a.getAnyTimeWindow());
        dto.setAnyVehicleCategory(a.getAnyVehicleCategory());
        dto.setAnyDriver(a.getAnyDriver());
        dto.setUpdatedBy(a.getUpdatedBy());
        dto.setUpdatedAt(a.getUpdatedAt());

        // Load grids
        dto.setTimeWindows(
                timeWindowRepository.findByAddressAddressCode(a.getAddressCode())
                        .stream()
                        .map(tw -> {
                            TimeWindowDTO t = new TimeWindowDTO();
                            t.setId(tw.getId());
                            t.setFromTime(tw.getFromTime());
                            t.setToTime(tw.getToTime());
                            t.setDisplayOrder(tw.getDisplayOrder());
                            return t;
                        }).toList()
        );

        dto.setVehicles(
                vehicleRepository.findByAddressAddressCode(a.getAddressCode())
                        .stream()
                        .map(v -> {
                            AddressVehicleDTO av = new AddressVehicleDTO();
                            av.setId(v.getId());
                            av.setVehicleCategoryCode(v.getVehicleCategoryCode());
                            return av;
                        }).toList()
        );

        dto.setDrivers(
                driverRepository.findByAddressAddressCode(a.getAddressCode())
                        .stream()
                        .map(d -> {
                            AddressDriverDTO ad = new AddressDriverDTO();
                            ad.setId(d.getId());
                            ad.setDriverId(d.getDriverId());
                            return ad;
                        }).toList()
        );

        return dto;
    }
}
