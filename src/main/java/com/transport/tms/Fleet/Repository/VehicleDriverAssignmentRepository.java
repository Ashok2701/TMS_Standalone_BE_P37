package com.transport.tms.Fleet.Repository;

import com.transport.tms.Fleet.Entity.VehicleDriverAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.UUID;

public interface VehicleDriverAssignmentRepository
        extends JpaRepository<VehicleDriverAssignment, UUID> {

    @Query("""
        SELECT COUNT(v)
        FROM VehicleDriverAssignment v
        WHERE v.driver.driverId = :driverId
        AND v.active = true
        AND (
            v.startDate <= :endDate
            AND COALESCE(v.endDate, :endDate) >= :startDate
        )
    """)
    Long countDriverOverlap(
            String driverId,
            LocalDate startDate,
            LocalDate endDate);

    @Query("""
        SELECT COUNT(v)
        FROM VehicleDriverAssignment v
        WHERE v.vehicle.vehicleCode = :vehicleCode
        AND v.active = true
        AND (
            v.startDate <= :endDate
            AND COALESCE(v.endDate, :endDate) >= :startDate
        )
    """)
    Long countVehicleOverlap(
            String vehicleCode,
            LocalDate startDate,
            LocalDate endDate);
}