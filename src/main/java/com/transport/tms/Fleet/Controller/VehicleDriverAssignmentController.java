package com.transport.tms.Fleet.Controller;

import com.transport.tms.Fleet.Dto.VehicleDriverAssignmentDTO;
import com.transport.tms.Fleet.Service.VehicleDriverAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vehicle-driver-assignment")
@RequiredArgsConstructor
public class VehicleDriverAssignmentController {

    private final VehicleDriverAssignmentService service;

    @PostMapping
    public VehicleDriverAssignmentDTO create(
            @RequestBody VehicleDriverAssignmentDTO dto) {

        return service.create(dto);
    }

    @PutMapping("/{assignmentId}")
    public VehicleDriverAssignmentDTO update(
            @PathVariable UUID assignmentId,
            @RequestBody VehicleDriverAssignmentDTO dto) {

        return service.update(
                assignmentId,
                dto);
    }

    @GetMapping("/{assignmentId}")
    public VehicleDriverAssignmentDTO getById(
            @PathVariable UUID assignmentId) {

        return service.getById(
                assignmentId);
    }

    @GetMapping
    public List<VehicleDriverAssignmentDTO> getAll() {

        return service.getAll();
    }

    @DeleteMapping("/{assignmentId}")
    public void delete(
            @PathVariable UUID assignmentId) {

        service.delete(
                assignmentId);
    }
}