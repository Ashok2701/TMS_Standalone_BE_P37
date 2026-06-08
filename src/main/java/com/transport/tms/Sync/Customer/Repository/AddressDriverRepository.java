package com.transport.tms.Sync.Customer.Repository;

import com.transport.tms.Sync.Customer.Entity.XRAddressDriver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AddressDriverRepository
        extends JpaRepository<XRAddressDriver, UUID> {

    List<XRAddressDriver> findByAddressCustomerCodeAndAddressAddressCode(
            String customerCode, String addressCode);

    void deleteByAddressCustomerCodeAndAddressAddressCode(
            String customerCode, String addressCode);
}
