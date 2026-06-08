package com.transport.tms.Sync.Customer.Repository;

import com.transport.tms.Sync.Customer.Entity.XRAddressVehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AddressVehicleRepository
        extends JpaRepository<XRAddressVehicle, UUID> {

    List<XRAddressVehicle> findByAddressAddressCode(String addressCode);

    void deleteByAddressAddressCode(String addressCode);
}
