package com.transport.tms.Sync.Customer.Repository;

import com.transport.tms.Sync.Customer.Entity.XRCustomerAddress;
import com.transport.tms.Sync.Customer.Entity.XRCustomerAddressId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerAddressRepository
        extends JpaRepository<XRCustomerAddress, XRCustomerAddressId> {

    // All addresses for a customer
    List<XRCustomerAddress> findByCustomerCode(String customerCode);

    // Count addresses for a customer (used in list view)
    Integer countByCustomerCode(String customerCode);

    // Find by composite key
    Optional<XRCustomerAddress> findByCustomerCodeAndAddressCode(
            String customerCode, String addressCode);

    // Delete all addresses for a customer (used in tests/cleanup)
    void deleteByCustomerCode(String customerCode);
}
