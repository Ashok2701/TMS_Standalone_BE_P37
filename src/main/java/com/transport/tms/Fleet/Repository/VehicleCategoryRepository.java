package com.transport.tms.Fleet.Repository;

import com.transport.tms.Fleet.Entity.VehicleCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleCategoryRepository
        extends JpaRepository<VehicleCategory, String> {

    boolean existsByCategoryCode(
            String categoryCode);
}