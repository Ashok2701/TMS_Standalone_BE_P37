package com.transport.tms.Sync.Customer.Service;

import com.transport.tms.Sync.Customer.Entity.XRCustomerAddress;
import com.transport.tms.Sync.Customer.Repository.CustomerAddressRepository;
import com.transport.tms.Sync.Dto.SyncResult;
import com.transport.tms.Sync.X3.Dto.X3CustomerAddressDTO;
import com.transport.tms.Sync.X3.Repository.X3CustomerAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerAddressSyncService {

    private final X3CustomerAddressRepository x3Repository;

    private final CustomerAddressRepository addressRepository;

    @Transactional
    public SyncResult sync() {

        Integer x3Count =
                x3Repository.count();

        Integer before =
                (int) addressRepository.count();

        List<X3CustomerAddressDTO> addresses =
                x3Repository.findCustomerAddresses();

        int inserted = 0;
        int updated  = 0;

        for (X3CustomerAddressDTO dto : addresses) {

            boolean exists =
                    addressRepository.existsById(
                            dto.getAddressCode());

            XRCustomerAddress address =
                    addressRepository
                            .findById(dto.getAddressCode())
                            .orElse(new XRCustomerAddress());

            address.setAddressCode(dto.getAddressCode());
            address.setCustomerCode(dto.getCustomerCode());
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

            addressRepository.save(address);

            if (exists) updated++;
            else         inserted++;
        }

        Integer after =
                (int) addressRepository.count();

        return new SyncResult(
                x3Count, before, after, inserted, updated, 0);
    }
}
