package com.transport.tms.Fleet.Service;

import com.transport.tms.Fleet.Dto.VehicleDTO;
import com.transport.tms.Fleet.Dto.BulkRowResult;

import java.util.List;

public interface VehicleService {

    VehicleDTO create(
            VehicleDTO dto);

    VehicleDTO update(
            String vehicleCode,
            VehicleDTO dto);

    VehicleDTO getById(
            String vehicleCode);

    List<VehicleDTO> getAll();

    void delete(
            String vehicleCode);

    /** Bulk create-or-update: each row creates a new vehicle if its
     *  vehicleCode doesn't exist yet, or updates the existing one if it
     *  does. Never throws for an individual row failure — collects
     *  per-row success/error into the returned list instead, so one bad
     *  row in a large import doesn't abort the rest. */
    List<BulkRowResult> bulkCreateOrUpdate(List<VehicleDTO> rows);
}