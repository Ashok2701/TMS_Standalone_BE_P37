package com.transport.tms.Fleet.Service;

import com.transport.tms.Fleet.Dto.VehicleDTO;

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
}