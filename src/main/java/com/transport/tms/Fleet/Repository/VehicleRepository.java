package com.transport.tms.Fleet.Repository;

import com.transport.tms.Fleet.Entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository
        extends JpaRepository<Vehicle, String> {

    boolean existsByVehicleCode(
            String vehicleCode);

    boolean existsByVehicleNumber(
            String vehicleNumber);
}