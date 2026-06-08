package com.transport.tms.Sync.Customer.Controller;

import com.transport.tms.Sync.Customer.Dto.*;
import com.transport.tms.Sync.Customer.Service.CustomerManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerManagementController {

    private final CustomerManagementService service;

    // ──────────────────────────────────────────────────────────
    // GET  /api/v1/customers
    // List view — lightweight rows with address count
    // ──────────────────────────────────────────────────────────
    @GetMapping
    public List<CustomerListDTO> list() {
        return service.listCustomers();
    }

    // ──────────────────────────────────────────────────────────
    // GET  /api/v1/customers/{customerCode}
    // Detail view — full customer + all addresses + all grids
    // ──────────────────────────────────────────────────────────
    @GetMapping("/{customerCode}")
    public CustomerDetailDTO getDetail(
            @PathVariable String customerCode) {

        return service.getCustomerDetail(customerCode);
    }

    // ──────────────────────────────────────────────────────────
    // PUT  /api/v1/customers/{customerCode}
    // Save Info tab TMS fields (lat/lng, serviceTime, waitingTime)
    // Returns full updated detail
    // ──────────────────────────────────────────────────────────
    @PutMapping("/{customerCode}")
    public CustomerDetailDTO updateTms(
            @PathVariable String customerCode,
            @RequestBody CustomerDetailDTO dto) {

        return service.updateCustomerTms(customerCode, dto);
    }

    // ──────────────────────────────────────────────────────────
    // GET  /api/v1/customers/{customerCode}/addresses/{addressCode}
    // Single address detail with grids
    // ──────────────────────────────────────────────────────────
    @GetMapping("/{customerCode}/addresses/{addressCode}")
    public CustomerAddressDetailDTO getAddress(
            @PathVariable String customerCode,
            @PathVariable String addressCode) {

        return service.getAddressDetail(addressCode);
    }

    // ──────────────────────────────────────────────────────────
    // PUT  /api/v1/customers/{customerCode}/addresses/{addressCode}
    // Save Address tab — TMS flags + all 3 grids (timeWindows, vehicles, drivers)
    // Grids are REPLACED completely on each save
    // Returns updated address detail
    // ──────────────────────────────────────────────────────────
    @PutMapping("/{customerCode}/addresses/{addressCode}")
    public CustomerAddressDetailDTO saveAddress(
            @PathVariable String customerCode,
            @PathVariable String addressCode,
            @RequestBody CustomerAddressDetailDTO dto) {

        return service.saveAddressTms(addressCode, dto);
    }
}
