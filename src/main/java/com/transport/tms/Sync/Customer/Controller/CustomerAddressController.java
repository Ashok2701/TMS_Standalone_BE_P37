package com.transport.tms.Sync.Customer.Controller;

import com.transport.tms.Sync.Customer.Entity.XRCustomerAddress;
import com.transport.tms.Sync.Customer.Repository.CustomerAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer-addresses")
@RequiredArgsConstructor
public class CustomerAddressController {

    private final CustomerAddressRepository repository;

    @GetMapping
    public List<XRCustomerAddress> getAll() {
        return repository.findAll();
    }

    @GetMapping("/{addressCode}")
    public XRCustomerAddress getById(
            @PathVariable String addressCode) {

        return repository.findById(addressCode)
                .orElseThrow(() ->
                        new RuntimeException("Address not found: " + addressCode));
    }

    @GetMapping("/by-customer/{customerCode}")
    public List<XRCustomerAddress> getByCustomer(
            @PathVariable String customerCode) {

        return repository.findByCustomerCode(customerCode);
    }
}
