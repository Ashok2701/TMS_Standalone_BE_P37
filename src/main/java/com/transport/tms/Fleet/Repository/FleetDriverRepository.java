package com.transport.tms.Fleet.Repository;

import com.transport.tms.Fleet.Entity.FleetDriver;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FleetDriverRepository extends CrudRepository<FleetDriver, String> {
    FleetDriver findByDriverId(String driverId);
    List<FleetDriver> findAll();
    void deleteByDriverId(String driverId);
    boolean existsByDriverId(String driverId);
}
