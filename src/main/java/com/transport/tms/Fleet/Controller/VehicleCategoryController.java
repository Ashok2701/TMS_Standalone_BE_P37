package com.transport.tms.Fleet.Controller;

import com.transport.tms.Fleet.Dto.VehicleCategoryDTO;
import com.transport.tms.Fleet.Service.VehicleCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicle-category")
@RequiredArgsConstructor
public class VehicleCategoryController {

    private final VehicleCategoryService service;

    @PostMapping
    public VehicleCategoryDTO create(
            @RequestBody VehicleCategoryDTO dto) {

        return service.create(dto);
    }

    @PutMapping("/{categoryCode}")
    public VehicleCategoryDTO update(
            @PathVariable String categoryCode,
            @RequestBody VehicleCategoryDTO dto) {

        return service.update(categoryCode, dto);
    }

    @GetMapping("/{categoryCode}")
    public VehicleCategoryDTO getById(
            @PathVariable String categoryCode) {

        return service.getById(categoryCode);
    }

    @GetMapping
    public List<VehicleCategoryDTO> getAll() {

        return service.getAll();
    }

    @DeleteMapping("/{categoryCode}")
    public void delete(
            @PathVariable String categoryCode) {

        service.delete(categoryCode);
    }
}