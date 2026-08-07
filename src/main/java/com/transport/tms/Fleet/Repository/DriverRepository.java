package com.transport.tms.Fleet.Repository;

import com.transport.tms.Fleet.Entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DriverRepository
        extends JpaRepository<Driver, String> {

    boolean existsByDriverId(
            String driverId);

    boolean existsByLicenseNumber(
            String licenseNumber);

    Optional<Driver> findByUsername(String username);

    boolean existsByUsername(String username);
}