package com.transport.tms.UserManagement.Controller;

import com.transport.tms.UserManagement.Dto.ModuleDTO;
import com.transport.tms.UserManagement.Service.ModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/modules")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ModuleController {

    private final ModuleService service;

    // CREATE

    @PostMapping
    public ModuleDTO create(
            @RequestBody ModuleDTO dto) {

        return service.create(dto);
    }

    // GET ALL

    @GetMapping
    public List<ModuleDTO> getAll() {

        return service.getAll();
    }

    // GET BY ID

    @GetMapping("/{id}")
    public ModuleDTO getById(
            @PathVariable UUID id) {

        return service.getById(id);
    }

    // UPDATE

    @PutMapping("/{id}")
    public ModuleDTO update(
            @PathVariable UUID id,

            @RequestBody ModuleDTO dto) {

        return service.update(id, dto);
    }

    // DELETE

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable UUID id) {

        service.delete(id);
    }
}