package com.transport.tms.Fleet.Service;

import com.transport.tms.Fleet.Dto.DriverDTO;

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
}