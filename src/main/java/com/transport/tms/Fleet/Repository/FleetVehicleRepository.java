package com.transport.tms.Fleet.Repository;

import com.transport.tms.Fleet.Entity.FleetVehicle;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FleetVehicleRepository extends CrudRepository<FleetVehicle, Long> {

    List<FleetVehicle> findAll();
    FleetVehicle findByCodeyve(String codeyve);
    void deleteByCodeyve(String codeyve);
    boolean existsByCodeyve(String codeyve);

}
