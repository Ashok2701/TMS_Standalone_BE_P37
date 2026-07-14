package com.transport.tms.RoutePlanner.Repository;

import com.transport.tms.Fleet.Entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoutePlannerDriverRepository
        extends JpaRepository<Driver, String> {

    @Query("SELECT d FROM Driver d WHERE d.active = true AND d.site = :siteCode ORDER BY d.driverName")
    List<Driver> findAllActiveDrivers(@Param("siteCode") String siteCode);
}
