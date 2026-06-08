package com.transport.tms.Sync.Customer.Controller;

import com.transport.tms.Sync.Customer.Entity.XRCustomer;
import com.transport.tms.Sync.Customer.Repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerRepository repository;

    @GetMapping
    public List<XRCustomer> getAll() {
        return repository.findAll();
    }

    @GetMapping("/{customerCode}")
    public XRCustomer getById(
            @PathVariable String customerCode) {

        return repository.findById(customerCode)
                .orElseThrow(() ->
                        new RuntimeException("Customer not found: " + customerCode));
    }
}
