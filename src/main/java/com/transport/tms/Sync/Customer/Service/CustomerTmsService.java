package com.transport.tms.Sync.Customer.Service;

import com.transport.tms.Sync.Customer.Dto.CustomerTmsDTO;
import com.transport.tms.Sync.Customer.Entity.XRCustomer;
import com.transport.tms.Sync.Customer.Repository.CustomerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CustomerTmsService {

    private final CustomerRepository repository;

    // ── GET TMS fields for a customer ──────────────────────────
    public CustomerTmsDTO getTmsFields(String customerCode) {

        XRCustomer customer = repository.findById(customerCode)
                .orElseThrow(() ->
                        new RuntimeException("Customer not found: " + customerCode));

        CustomerTmsDTO dto = new CustomerTmsDTO();
        dto.setLatitude(customer.getLatitude());
        dto.setLongitude(customer.getLongitude());
        dto.setServiceTime(customer.getServiceTime());
        dto.setWaitingTime(customer.getWaitingTime());

        return dto;
    }

    // ── UPDATE TMS fields only ──────────────────────────────────
    @Transactional
    public CustomerTmsDTO updateTmsFields(String customerCode,
                                          CustomerTmsDTO dto) {

        XRCustomer customer = repository.findById(customerCode)
                .orElseThrow(() ->
                        new RuntimeException("Customer not found: " + customerCode));

        // Only TMS fields — never touch X3 fields
        customer.setLatitude(dto.getLatitude());
        customer.setLongitude(dto.getLongitude());
        customer.setServiceTime(dto.getServiceTime());
        customer.setWaitingTime(dto.getWaitingTime());
        customer.setUpdatedBy(dto.getUpdatedBy());
        customer.setUpdatedAt(LocalDateTime.now());

        repository.save(customer);

        return getTmsFields(customerCode);
    }
}
