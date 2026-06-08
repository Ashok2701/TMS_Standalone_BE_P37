package com.transport.tms.Fleet.Controller;

import com.transport.tms.Fleet.Dto.VehicleDTO;
import com.transport.tms.Fleet.Service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService service;

    @PostMapping
    public VehicleDTO create(
            @RequestBody VehicleDTO dto) {

        return service.create(dto);
    }

    @PutMapping("/{vehicleCode}")
    public VehicleDTO update(
            @PathVariable String vehicleCode,
            @RequestBody VehicleDTO dto) {

        return service.update(
                vehicleCode,
                dto);
    }

    @GetMapping("/{vehicleCode}")
    public VehicleDTO getById(
            @PathVariable String vehicleCode) {

        return service.getById(vehicleCode);
    }

    @GetMapping
    public List<VehicleDTO> getAll() {

        return service.getAll();
    }

    @DeleteMapping("/{vehicleCode}")
    public void delete(
            @PathVariable String vehicleCode) {

        service.delete(vehicleCode);
    }
}