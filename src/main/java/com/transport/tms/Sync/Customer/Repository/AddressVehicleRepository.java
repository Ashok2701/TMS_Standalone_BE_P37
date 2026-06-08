package com.transport.tms.Sync.Customer.Repository;

import com.transport.tms.Sync.Customer.Entity.XRAddressVehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AddressVehicleRepository
        extends JpaRepository<XRAddressVehicle, UUID> {

    List<XRAddressVehicle> findByAddressCustomerCodeAndAddressAddressCode(
            String customerCode, String addressCode);

    void deleteByAddressCustomerCodeAndAddressAddressCode(
            String customerCode, String addressCode);
}
