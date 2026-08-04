package com.transport.tms.Fleet.Repository;

import com.transport.tms.Fleet.Entity.VehicleDriverAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.UUID;

public interface VehicleDriverAssignmentRepository
        extends JpaRepository<VehicleDriverAssignment, UUID> {

    // BUG FIX: without excludeAssignmentId, updating an assignment always
    // conflicted with ITSELF — the overlap check had no way to know "this
    // assignment" isn't a competing one. Pass null from create() (nothing
    // to exclude yet) and the entity's own ID from update().
    @Query("""
        SELECT COUNT(v)
        FROM VehicleDriverAssignment v
        WHERE v.driver.driverId = :driverId
        AND v.active = true
        AND (:excludeAssignmentId IS NULL OR v.assignmentId <> :excludeAssignmentId)
        AND (
            v.startDate <= :endDate
            AND COALESCE(v.endDate, :endDate) >= :startDate
        )
    """)
    Long countDriverOverlap(
            String driverId,
            LocalDate startDate,
            LocalDate endDate,
            UUID excludeAssignmentId);

    @Query("""
        SELECT COUNT(v)
        FROM VehicleDriverAssignment v
        WHERE v.vehicle.vehicleCode = :vehicleCode
        AND v.active = true
        AND (:excludeAssignmentId IS NULL OR v.assignmentId <> :excludeAssignmentId)
        AND (
            v.startDate <= :endDate
            AND COALESCE(v.endDate, :endDate) >= :startDate
        )
    """)
    Long countVehicleOverlap(
            String vehicleCode,
            LocalDate startDate,
            LocalDate endDate,
            UUID excludeAssignmentId);
}