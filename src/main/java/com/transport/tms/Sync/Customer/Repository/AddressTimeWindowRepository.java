package com.transport.tms.Sync.Customer.Repository;

import com.transport.tms.Sync.Customer.Entity.XRAddressTimeWindow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AddressTimeWindowRepository
        extends JpaRepository<XRAddressTimeWindow, UUID> {

    List<XRAddressTimeWindow> findByAddressCustomerCodeAndAddressAddressCode(
            String customerCode, String addressCode);

    void deleteByAddressCustomerCodeAndAddressAddressCode(
            String customerCode, String addressCode);
}
