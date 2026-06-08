package com.transport.tms.Fleet.Service;

import com.transport.tms.Fleet.Dto.VehicleCategoryDTO;

import java.util.List;

public interface VehicleCategoryService {

    VehicleCategoryDTO create(
            VehicleCategoryDTO dto);

    VehicleCategoryDTO update(
            String categoryCode,
            VehicleCategoryDTO dto);

    VehicleCategoryDTO getById(
            String categoryCode);

    List<VehicleCategoryDTO> getAll();

    void delete(
            String categoryCode);
}