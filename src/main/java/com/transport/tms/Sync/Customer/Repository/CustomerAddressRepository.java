package com.transport.tms.Sync.Customer.Repository;

import com.transport.tms.Sync.Customer.Entity.XRCustomerAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerAddressRepository
        extends JpaRepository<XRCustomerAddress, String> {

    List<XRCustomerAddress> findByCustomerCode(String customerCode);

    void deleteByCustomerCode(String customerCode);

    Integer countByCustomerCode(String customerCode);
}
