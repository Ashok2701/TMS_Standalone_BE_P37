package com.transport.tms.Fleet.Service;

import com.transport.tms.Fleet.Dto.VehicleDriverAssignmentDTO;

import java.util.List;
import java.util.UUID;

public interface VehicleDriverAssignmentService {

    VehicleDriverAssignmentDTO create(
            VehicleDriverAssignmentDTO dto);

    VehicleDriverAssignmentDTO update(
            UUID assignmentId,
            VehicleDriverAssignmentDTO dto);

    VehicleDriverAssignmentDTO getById(
            UUID assignmentId);

    List<VehicleDriverAssignmentDTO> getAll();

    void delete(
            UUID assignmentId);
}