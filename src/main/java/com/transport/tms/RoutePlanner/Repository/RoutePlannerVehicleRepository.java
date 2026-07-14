package com.transport.tms.RoutePlanner.Repository;

import com.transport.tms.Fleet.Entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoutePlannerVehicleRepository
        extends JpaRepository<Vehicle, String> {

    @Query("SELECT v FROM Vehicle v WHERE v.active = true AND v.site = :siteCode ORDER BY v.vehicleCode")
    List<Vehicle> findAllActiveVehicles(@Param("siteCode") String siteCode);
}
