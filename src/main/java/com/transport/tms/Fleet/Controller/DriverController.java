package com.transport.tms.Fleet.Controller;

import com.transport.tms.Fleet.Dto.DriverDTO;
import com.transport.tms.Fleet.Entity.DropdownData;
import com.transport.tms.Fleet.Repository.CommonRepository;
import com.transport.tms.Fleet.Service.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService service;
    private final CommonRepository commonRepository;

    @PostMapping
    public DriverDTO create(@RequestBody DriverDTO dto) {
        return service.create(dto);
    }

    @PutMapping("/{driverId}")
    public DriverDTO update(
            @PathVariable String driverId,
            @RequestBody DriverDTO dto) {
        return service.update(driverId, dto);
    }

    @GetMapping("/{driverId}")
    public DriverDTO getById(@PathVariable String driverId) {
        return service.getById(driverId);
    }

    // Returns full TMS driver detail (license, status, hours, etc.)
    @GetMapping
    public List<DriverDTO> getAll() {
        return service.getAll();
    }

    // X3 raw dropdown values (value/label only) — kept for legacy dropdowns
    @GetMapping("/x3-list")
    public List<DropdownData> getX3List() {
        return commonRepository.getDriverList();
    }

    @DeleteMapping("/{driverId}")
    public void delete(@PathVariable String driverId) {
        service.delete(driverId);
    }
}
