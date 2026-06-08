package com.transport.tms.Sync.Customer.Service;

import com.transport.tms.Sync.Customer.Entity.XRCustomerAddress;
import com.transport.tms.Sync.Customer.Entity.XRCustomerAddressId;
import com.transport.tms.Sync.Customer.Repository.CustomerAddressRepository;
import com.transport.tms.Sync.Dto.SyncResult;
import com.transport.tms.Sync.X3.Dto.X3CustomerAddressDTO;
import com.transport.tms.Sync.X3.Repository.X3CustomerAddressRepository;
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
public class CustomerAddressSyncService {

    private final X3CustomerAddressRepository x3Repository;
    private final CustomerAddressRepository addressRepository;

    @Transactional
    public SyncResult sync() {

        System.out.println("======================");
        System.out.println("CUSTOMER ADDRESS SYNC STARTED");
        System.out.println("======================");

        Integer x3Count = x3Repository.count();
        System.out.println("X3 count = " + x3Count);

        Integer before = (int) addressRepository.count();

        // KEY FIX: map keyed by "customerCode::addressCode" — not just addressCode
        // BPAADD_0 ("10", "001" etc) repeats across customers — NOT globally unique
        Map<String, XRCustomerAddress> existingMap =
                addressRepository.findAll()
                        .stream()
                        .collect(Collectors.toMap(
                                a -> a.getCustomerCode() + "::" + a.getAddressCode(),
                                a -> a
                        ));

        List<X3CustomerAddressDTO> addresses = x3Repository.findCustomerAddresses();
        System.out.println("X3 CUSTOMER ADDRESSES FETCHED = " + addresses.size());

        int inserted = 0;
        int updated  = 0;
        int skipped  = 0;

        for (X3CustomerAddressDTO dto : addresses) {

            // Composite lookup key
            String compositeKey = dto.getCustomerCode() + "::" + dto.getAddressCode();
            XRCustomerAddress existing = existingMap.get(compositeKey);

            if (existing == null) {

                // NEW — insert
                XRCustomerAddress address = new XRCustomerAddress();
                mapX3ToEntity(dto, address);
                addressRepository.save(address);
                inserted++;

            } else if (hasChanged(dto, existing)) {

                // CHANGED — update
                mapX3ToEntity(dto, existing);
                addressRepository.save(existing);
                updated++;

            } else {

                // UNCHANGED — skip
                skipped++;
            }
        }

        Integer after = (int) addressRepository.count();

        System.out.println("CUSTOMER ADDRESS SYNC DONE — inserted=" + inserted
                + " updated=" + updated + " skipped=" + skipped);

        return new SyncResult(x3Count, before, after, inserted, updated, 0);
    }

    private void mapX3ToEntity(X3CustomerAddressDTO dto, XRCustomerAddress address) {
        address.setCustomerCode(dto.getCustomerCode());
        address.setAddressCode(dto.getAddressCode());
        address.setAddressDescription(dto.getAddressDescription());
        address.setAddressLine1(dto.getAddressLine1());
        address.setAddressLine2(dto.getAddressLine2());
        address.setAddressLine3(dto.getAddressLine3());
        address.setCity(dto.getCity());
        address.setPostalCode(dto.getPostalCode());
        address.setStateCode(dto.getStateCode());
        address.setCountryCode(dto.getCountryCode());
        address.setCountryName(dto.getCountryName());
        address.setPhone(dto.getPhone());
        address.setMobile(dto.getMobile());
        address.setWebSite(dto.getWebSite());
        address.setDefaultAddress(dto.getDefaultAddress());
        address.setSyncedAt(LocalDateTime.now());
    }

    private boolean hasChanged(X3CustomerAddressDTO dto, XRCustomerAddress existing) {
        return !eq(dto.getAddressDescription(), existing.getAddressDescription())
            || !eq(dto.getAddressLine1(),       existing.getAddressLine1())
            || !eq(dto.getAddressLine2(),       existing.getAddressLine2())
            || !eq(dto.getAddressLine3(),       existing.getAddressLine3())
            || !eq(dto.getCity(),               existing.getCity())
            || !eq(dto.getPostalCode(),         existing.getPostalCode())
            || !eq(dto.getStateCode(),          existing.getStateCode())
            || !eq(dto.getCountryCode(),        existing.getCountryCode())
            || !eq(dto.getCountryName(),        existing.getCountryName())
            || !eq(dto.getPhone(),              existing.getPhone())
            || !eq(dto.getMobile(),             existing.getMobile())
            || !eq(dto.getWebSite(),            existing.getWebSite());
    }

    private boolean eq(String a, String b) {
        return Objects.equals(
                a == null ? "" : a.trim(),
                b == null ? "" : b.trim());
    }
}
