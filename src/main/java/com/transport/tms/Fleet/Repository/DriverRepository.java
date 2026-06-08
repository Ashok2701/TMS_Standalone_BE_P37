package com.transport.tms.Fleet.Repository;

import com.transport.tms.Fleet.Entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DriverRepository
        extends JpaRepository<Driver, String> {

    boolean existsByDriverId(
            String driverId);

    boolean existsByLicenseNumber(
            String licenseNumber);
}