package com.transport.tms.Sync.Customer.Service;

import com.transport.tms.Sync.Customer.Entity.XRCustomer;
import com.transport.tms.Sync.Customer.Repository.CustomerRepository;
import com.transport.tms.Sync.Dto.SyncResult;
import com.transport.tms.Sync.X3.Dto.X3CustomerDTO;
import com.transport.tms.Sync.X3.Repository.X3CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerSyncService {

    private final X3CustomerRepository x3Repository;

    private final CustomerRepository customerRepository;

    @Transactional
    public SyncResult sync() {

        System.out.println("======================");
        System.out.println("CUSTOMER SYNC STARTED");
        System.out.println("======================");

        Integer x3Count = x3Repository.count();
        System.out.println("X3 count = " + x3Count);

        Integer before = (int) customerRepository.count();

        // Load all existing customers into map — single DB query
        Map<String, XRCustomer> existingMap =
                customerRepository.findAll()
                        .stream()
                        .collect(Collectors.toMap(
                                XRCustomer::getCustomerCode,
                                c -> c
                        ));

        List<X3CustomerDTO> customers = x3Repository.findCustomers();
        System.out.println("X3 CUSTOMERS FETCHED = " + customers.size());

        int inserted = 0;
        int updated  = 0;
        int skipped  = 0;

        for (X3CustomerDTO dto : customers) {

            XRCustomer existing = existingMap.get(dto.getCustomerCode());

            if (existing == null) {

                // NEW — insert
                XRCustomer customer = new XRCustomer();
                mapX3ToEntity(dto, customer);
                customerRepository.save(customer);
                inserted++;

            } else if (hasChanged(dto, existing)) {

                // CHANGED — update
                mapX3ToEntity(dto, existing);
                customerRepository.save(existing);
                updated++;

            } else {

                // UNCHANGED — skip
                skipped++;
            }
        }

        // ── DEACTIVATE records no longer in X3 ─────────────
        // Build set of all codes that came from X3 this sync
        java.util.Set<String> x3Codes = customers.stream()
                .map(X3CustomerDTO::getCustomerCode)
                .collect(java.util.stream.Collectors.toSet());

        int deactivated = 0;
        for (Map.Entry<String, XRCustomer> entry : existingMap.entrySet()) {
            if (!x3Codes.contains(entry.getKey())) {
                XRCustomer gone = entry.getValue();
                if (Boolean.TRUE.equals(gone.getActive())) {
                    gone.setActive(false);
                    gone.setSyncedAt(java.time.LocalDateTime.now());
                    customerRepository.save(gone);
                    deactivated++;
                    System.out.println("DEACTIVATED customer: " + gone.getCustomerCode());
                }
            }
        }

        Integer after = (int) customerRepository.count();

        System.out.println("CUSTOMER SYNC DONE — inserted=" + inserted
                + " updated=" + updated
                + " skipped=" + skipped
                + " deactivated=" + deactivated);

        return new SyncResult(x3Count, before, after, inserted, updated, 0);
    }

    private void mapX3ToEntity(X3CustomerDTO dto, XRCustomer customer) {

        customer.setCustomerCode(dto.getCustomerCode());
        customer.setCustomerName(dto.getCustomerName());
        customer.setShortName(dto.getShortName());
        customer.setCountryCode(dto.getCountryCode());
        customer.setCurrencyCode(dto.getCurrencyCode());
        customer.setActive(dto.getActive());
        customer.setSyncedAt(LocalDateTime.now());
    }

    private boolean hasChanged(X3CustomerDTO dto, XRCustomer existing) {

        return !eq(dto.getCustomerName(),  existing.getCustomerName())
            || !eq(dto.getShortName(),     existing.getShortName())
            || !eq(dto.getCountryCode(),   existing.getCountryCode())
            || !eq(dto.getCurrencyCode(),  existing.getCurrencyCode())
            || !Objects.equals(dto.getActive(), existing.getActive());
    }

    private boolean eq(String a, String b) {
        return Objects.equals(
                a == null ? "" : a.trim(),
                b == null ? "" : b.trim()
        );
    }
}
