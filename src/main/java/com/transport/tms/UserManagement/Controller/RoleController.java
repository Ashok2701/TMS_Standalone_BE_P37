package com.transport.tms.UserManagement.Controller;


import com.transport.tms.UserManagement.Dto.RoleDTO;
import com.transport.tms.UserManagement.Service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@CrossOrigin("*")
public class RoleController {

    private final RoleService service;

    @PostMapping
    public RoleDTO create(
            @RequestBody RoleDTO dto) {

        return service.create(dto);
    }

    @GetMapping
    public List<RoleDTO> getAll() {

        return service.getAll();
    }

    @GetMapping("/{id}")
    public RoleDTO getById(
            @PathVariable UUID id) {

        return service.getById(id);
    }

    @PutMapping("/{id}")
    public RoleDTO update(
            @PathVariable UUID id,
            @RequestBody RoleDTO dto) {

        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable UUID id) {

        service.delete(id);
    }
}