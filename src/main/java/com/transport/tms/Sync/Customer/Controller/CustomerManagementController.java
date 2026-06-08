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

    @GetMapping
    public List<CustomerListDTO> list() {
        return service.listCustomers();
    }

    @GetMapping("/{customerCode}")
    public CustomerDetailDTO getDetail(
            @PathVariable String customerCode) {
        return service.getCustomerDetail(customerCode);
    }

    @PutMapping("/{customerCode}")
    public CustomerDetailDTO updateTms(
            @PathVariable String customerCode,
            @RequestBody CustomerDetailDTO dto) {
        return service.updateCustomerTms(customerCode, dto);
    }

    @GetMapping("/{customerCode}/addresses/{addressCode}")
    public CustomerAddressDetailDTO getAddress(
            @PathVariable String customerCode,
            @PathVariable String addressCode) {
        return service.getAddressDetail(customerCode, addressCode);
    }

    @PutMapping("/{customerCode}/addresses/{addressCode}")
    public CustomerAddressDetailDTO saveAddress(
            @PathVariable String customerCode,
            @PathVariable String addressCode,
            @RequestBody CustomerAddressDetailDTO dto) {
        return service.saveAddressTms(customerCode, addressCode, dto);
    }
}
