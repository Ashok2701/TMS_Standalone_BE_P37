package com.transport.tms.Fleet.Service;

import com.transport.tms.Fleet.Dto.DriverDTO;
import com.transport.tms.Fleet.Dto.BulkRowResult;

import java.util.List;

public interface DriverService {

    DriverDTO create(
            DriverDTO dto);

    DriverDTO update(
            String driverId,
            DriverDTO dto);

    DriverDTO getById(
            String driverId);

    List<DriverDTO> getAll();

    void delete(
            String driverId);

    /** Bulk create-or-update, same semantics as VehicleService — see there. */
    List<BulkRowResult> bulkCreateOrUpdate(List<DriverDTO> rows);
}